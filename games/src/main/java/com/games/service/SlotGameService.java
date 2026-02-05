package com.games.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.config.GameProperties;
import com.games.config.SnowflakeIdGenerator;
import com.games.constant.GlobeConstant;
import com.games.constant.RedisConstant;
import com.games.dto.BetRecordsRequest;
import com.games.dto.BetRecordsResponse;
import com.games.dto.BetResponse;
import com.games.dto.RtpUpdateMessage;
import com.games.entity.Bet;
import com.games.entity.User;
import com.games.lock.RedisLock;
import com.games.repository.BetRepository;
import com.games.repository.UserRepository;
import com.games.rocketmq.producer.MessageProducerService;
import com.games.util.PageDataResUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotGameService {

    private final BetRepository betRepository;
    private final WalletService walletService;
    private final MessageProducerService messageProducerService;
    private final SnowflakeIdGenerator idGenerator;
    private final RedisLock redisLock;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public BetResponse placeBet(User user, String gameCode, BigDecimal betAmount) throws JsonProcessingException {
        final Long userId = user.getId();  // 保存到 final 变量
        String lockKey = GlobeConstant.USER + GlobeConstant.SEMICOLON
                + RedisConstant.PLACE_BET + userId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLock.tryLockWithRetry(lockKey,
                lockValue, 30, 3);

        if (!locked){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not acquire lock " + lockKey);
        }

        try {

            String json = (String) redisTemplate.opsForHash().get(RedisConstant.GAME_SETTING_ALL, gameCode);
            GameProperties gameProperties = objectMapper.readValue(json, GameProperties.class);
            // 使用悲观锁重新查询用户，确保数据库层面的并发安全
            User lockedUser = userRepository.findByIdWithLock(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            if (betAmount.compareTo(gameProperties.getMinBet()) < 0) {
                throw new RuntimeException("Bet amount is below minimum: " + gameProperties.getMinBet());
            }

            if (betAmount.compareTo(gameProperties.getMaxBet()) > 0) {
                throw new RuntimeException("Bet amount exceeds maximum: " + gameProperties.getMaxBet());
            }

            if (lockedUser.getBalance().compareTo(betAmount) < 0) {
                throw new RuntimeException("Insufficient balance");
            }

            BigDecimal balanceBefore = lockedUser.getBalance();

            List<String> spinLt = spin(gameProperties);

            BigDecimal winAmount = calculateWinAmount(spinLt, betAmount, gameProperties);
            boolean isWin = winAmount.compareTo(BigDecimal.ZERO) > 0;

            Bet bet = new Bet();
            bet.setId(idGenerator.nextId());
            bet.setUser(lockedUser);
            bet.setBetAmount(betAmount);
            bet.setWinAmount(winAmount);
            Map<String, String> symbolDisplay = gameProperties.getSymbolDisplay();
            List<String> spinResult = new ArrayList<>();
            for (String symbol : spinLt) {
                spinResult.add(symbolDisplay.get(symbol));
            }
            bet.setResult(String.join(",", spinResult));
            bet.setIsWin(isWin);
            bet.setGameCode(gameCode);
            bet = betRepository.save(bet);

            walletService.deductBalance(lockedUser, betAmount, bet, "Slot game bet");

            if (isWin) {
                walletService.addBalance(lockedUser, winAmount, bet, "Slot game win");
            }

            // 使用异步消息更新 RTP 统计，提高性能
            RtpUpdateMessage rtpMessage = new RtpUpdateMessage(betAmount, winAmount, gameCode);
            messageProducerService.sendRtpUpdateMessage(rtpMessage);

            BigDecimal balanceAfter = lockedUser.getBalance();

            String message = isWin
                    ? "Congratulations! You won " + winAmount + "!"
                    : "Better luck next time!";

            return new BetResponse(
                    bet.getId(),
                    spinResult,
                    betAmount,
                    winAmount,
                    isWin,
                    balanceBefore,
                    balanceAfter,
                    message
            );
        } finally {
            redisLock.releaseLock(lockKey, lockValue);
        }
    }

    private List<String> spin(GameProperties gameProperties) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            String symbol = getWeightedRandomSymbol(gameProperties);
            result.add(symbol);
        }

        log.debug("Spin result: {}", result);
        return result;
    }

    private String getWeightedRandomSymbol(GameProperties gameProperties) {
        Map<String, Double> weights = gameProperties.getSymbolWeights();

        // 計算總權重
        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();

        // 生成隨機數 [0, totalWeight)
        double randomValue = ThreadLocalRandom.current().nextDouble(totalWeight);

        log.debug("Total weight: {}, Random value: {}", totalWeight, randomValue);

        // 累加權重找到對應符號
        double currentWeight = 0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            currentWeight += entry.getValue();
            if (randomValue < currentWeight) {
                log.debug("Selected symbol: {} (weight: {}, accumulated: {})",
                    entry.getKey(), entry.getValue(), currentWeight);
                return entry.getKey();
            }
        }

        // 備用：返回第一個符號（理論上不應到達這裡）
        log.warn("Failed to select symbol by weight, using fallback");
        return gameProperties.getSymbols().get(0);
    }

    private BigDecimal calculateWinAmount(List<String> result, BigDecimal betAmount,
                                          GameProperties gameProperties) {
        String first = result.get(0);
        String second = result.get(1);
        String third = result.get(2);

        Map<String, Double> payoutMultipliers = gameProperties.getPayoutMultipliers();

        if (first.equals(second) && second.equals(third)) {
            Double multiplier = payoutMultipliers.get(first);
            if (multiplier != null) {
                BigDecimal winAmount = betAmount.multiply(BigDecimal.valueOf(multiplier));
                log.debug("Three matches of {}: win amount = {} ({}x)", first, winAmount, multiplier);
                return winAmount;
            }
        }

        if (first.equals(second) || second.equals(third) || first.equals(third)) {
            BigDecimal winAmount = betAmount.multiply(gameProperties.getTwoMatchMultiplier());
            log.debug("Two matches: win amount = {} ({}x)", winAmount, gameProperties.getTwoMatchMultiplier());
            return winAmount;
        }

        log.debug("No matches: no win");
        return BigDecimal.ZERO;
    }

    public PageDataResUtil<BetRecordsResponse> getBetRecords(User user, BetRecordsRequest request) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User required");
        }

        int pageNum = request.getPageNum() <= 0 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() <= 0 ? 1000 : request.getPageSize();

        Pageable pageable = PageRequest.of(
                pageNum - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "id")
        );

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(request.getStartTime(), formatter);
        LocalDateTime endTime = LocalDateTime.parse(request.getEndTime(), formatter);

        Page<Bet> betPage;
        if (request.getGameCode() != null && !request.getGameCode().trim().isEmpty()) {
            betPage = betRepository.findByUserAndGameCode(user, request.getGameCode(),
                    startTime, endTime, pageable);
        } else {
            betPage = betRepository.findByUser(user, startTime, endTime, pageable);
        }

        List<BetRecordsResponse> records = betPage.getContent().stream().map(bet -> {
            BetRecordsResponse resp = new BetRecordsResponse();
            // map basic fields; adapt setters/field names to your DTO if needed
            resp.setId(bet.getId());
            resp.setGameCode(bet.getGameCode());
            resp.setBetAmount(bet.getBetAmount());
            resp.setWinAmount(bet.getWinAmount());
            resp.setIsWin(bet.getIsWin());
            resp.setCreatedAt(bet.getCreatedAt());
            // convert stored CSV result to list
            if (bet.getResult() != null && !bet.getResult().isEmpty()) {
                resp.setResult(Arrays.asList(bet.getResult().split(",")));
            } else {
                resp.setResult(Collections.emptyList());
            }
            return resp;
        }).collect(Collectors.toList());

        PageDataResUtil<BetRecordsResponse> pageData = new PageDataResUtil<>();
        // adapt setter names to your PageDataResUtil implementation if they differ
        pageData.setList(records);
        pageData.setTotal(betPage.getTotalElements());
        pageData.setPage(pageNum);
        pageData.setSize(pageSize);
        pageData.setTotalPages(betPage.getTotalPages());

        return pageData;
    }
}
