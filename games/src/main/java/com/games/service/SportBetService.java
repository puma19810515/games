package com.games.service;

import com.games.constant.GlobeConstant;
import com.games.constant.RedisConstant;
import com.games.dto.*;
import com.games.entity.*;
import com.games.enums.*;
import com.games.lock.RedisLock;
import com.games.repository.*;
import com.games.rocketmq.producer.MessageProducerService;
import com.games.util.PageDataResUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 體育投注服務
 *
 * 功能：
 * 1. 單注投注（SINGLE）
 * 2. 串關投注（PARLAY）
 * 3. 投注記錄查詢
 * 4. 投注結算（含半贏半輸）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SportBetService {

    private final SportBetRepository sportBetRepository;
    private final BetLegRepository betLegRepository;
    private final SportEventRepository sportEventRepository;
    private final MarketLineRepository marketLineRepository;
    private final UserRepository userRepository;
    private final SportTransactionRepository sportTransactionRepository;
    private final RedisLock redisLock;
    private final MessageProducerService messageProducerService;

    // 最小串關腿數
    private static final int MIN_PARLAY_LEGS = 2;
    // 最大串關腿數
    private static final int MAX_PARLAY_LEGS = 10;
    // 最小投注金額
    private static final BigDecimal MIN_STAKE = new BigDecimal("1.00");
    // 最大投注金額
    private static final BigDecimal MAX_STAKE = new BigDecimal("100000.00");

    // ==================== Cashout 相關常數 ====================
    // 提前兌現手續費比例（5%）
    private static final BigDecimal CASHOUT_FEE_RATE = new BigDecimal("0.05");
    // 投注後可取消時間（分鐘）
    private static final int CANCEL_ALLOWED_MINUTES = 5;
    // Cashout 報價有效期（秒）
    private static final int CASHOUT_QUOTE_VALID_SECONDS = 30;

    /**
     * 下注（單注或串關）
     */
    @Transactional
    public SportBetResponse placeBet(Merchant merchant, User user, SportBetRequest request) {
        Long userId = user.getId();
        String lockKey = GlobeConstant.USER + GlobeConstant.SEMICOLON + merchant.getApiKey()
                + GlobeConstant.SEMICOLON + RedisConstant.PLACE_BET + "SPORT:" + userId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLock.tryLockWithRetry(lockKey, lockValue, 30, 3);
        if (!locked) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "系統繁忙，請稍後再試");
        }

        try {
            // 驗證投注類型
            SportBetType betType = SportBetType.valueOf(request.getBetType());

            // 驗證投注腿數
            validateLegCount(betType, request.getLegs().size());

            // 驗證投注金額
            validateStake(request.getStake());

            // 使用悲觀鎖重新查詢用戶
            User lockedUser = userRepository.findByIdWithLock(merchant.getId(), userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用戶不存在"));

            // 驗證餘額
            if (lockedUser.getSportBalance().compareTo(request.getStake()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "餘額不足");
            }

            // 驗證並處理每一腿
            List<BetLegData> legDataList = validateAndProcessLegs(request.getLegs(), betType);

            // 計算總賠率
            BigDecimal totalOdds = calculateTotalOdds(legDataList, betType);

            // 計算預計贏取金額
            BigDecimal potentialWin = calculatePotentialWin(request.getStake(), totalOdds);

            // 扣除餘額
            BigDecimal balanceBefore = lockedUser.getSportBalance();
            BigDecimal balanceAfter = balanceBefore.subtract(request.getStake());
            lockedUser.setSportBalance(balanceAfter);
            userRepository.save(lockedUser);

            // 建立投注主單
            SportBet sportBet = SportBet.builder()
                    .merchant(merchant)
                    .user(lockedUser)
                    .betType(betType)
                    .stake(request.getStake())
                    .totalOdds(totalOdds)
                    .potentialWin(potentialWin)
                    .winAmount(BigDecimal.ZERO)
                    .validBet(request.getStake())
                    .status(SportBetStatus.PENDING)
                    .placedAt(LocalDateTime.now())
                    .build();
            sportBetRepository.save(sportBet);

            // 建立投注腿
            List<BetLeg> betLegs = createBetLegs(sportBet, legDataList);
            betLegRepository.saveAll(betLegs);

            // 記錄交易流水
            SportTransaction transaction = SportTransaction.builder()
                    .merchant(merchant)
                    .user(lockedUser)
                    .sportBet(sportBet)
                    .type(SportTransactionType.SPORT_BET)
                    .amount(request.getStake().negate())
                    .balanceBefore(balanceBefore)
                    .balanceAfter(balanceAfter)
                    .description("體育投注 - " + betType.name())
                    .build();
            sportTransactionRepository.save(transaction);

            // 發送MQ消息
            sendBetMessage(sportBet, transaction);

            // 建立響應
            return buildBetResponse(sportBet, betLegs, balanceAfter);

        } finally {
            redisLock.releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 查詢投注記錄
     */
    public PageDataResUtil<SportBetRecordResponse> getBetRecords(User user, SportBetRecordRequest request) {
        SportBetType betType = null;
        SportBetStatus status = null;

        if (request.getBetType() != null) {
            betType = SportBetType.valueOf(request.getBetType());
        }
        if (request.getStatus() != null) {
            status = SportBetStatus.valueOf(request.getStatus());
        }

        Page<SportBet> page = this.findByUserIdWithFilters(
                user.getId(),
                betType,
                status,
                request.getStartTime(),
                request.getEndTime(),
                PageRequest.of(request.getPage(), request.getSize())
        );

        List<SportBetRecordResponse> records = page.getContent().stream()
                .map(this::buildBetRecordResponse)
                .collect(Collectors.toList());

        return PageDataResUtil.<SportBetRecordResponse>builder()
                .list(records)
                .page(request.getPage())
                .size(request.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    public Page<SportBet> findByUserIdWithFilters(Long userId, SportBetType betType,
                                                  SportBetStatus status, LocalDateTime startTime,
                                                  LocalDateTime endTime, Pageable pageable) {

        Specification<SportBet> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (betType != null) {
                predicates.add(cb.equal(root.get("betType"), betType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("placedAt"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("placedAt"), endTime));
            }

            query.orderBy(cb.desc(root.get("placedAt")));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return sportBetRepository.findAll(spec, pageable);
    }

    /**
     * 查詢單筆投注詳情
     */
    public SportBetRecordResponse getBetDetail(User user, Long betId) {
        SportBet sportBet = sportBetRepository.findByIdAndUserId(betId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投注單不存在"));
        return buildBetRecordResponse(sportBet);
    }

    /**
     * 結算投注（由排程或管理後台呼叫）
     */
    @Transactional
    public void settleBet(Long betId) {
        SportBet sportBet = sportBetRepository.findById(betId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投注單不存在"));

        if (sportBet.getStatus() != SportBetStatus.PENDING) {
            log.warn("投注單 {} 狀態非待結算，跳過", betId);
            return;
        }

        List<BetLeg> legs = betLegRepository.findByBetIdWithDetails(betId);

        // 檢查所有腿是否都已有結果
        boolean allSettled = legs.stream()
                .allMatch(leg -> leg.getResult() != SettlementResult.PENDING);

        if (!allSettled) {
            log.info("投注單 {} 尚有未結算的腿，跳過", betId);
            return;
        }

        // 計算贏取金額
        BigDecimal winAmount = calculateWinAmount(sportBet, legs);

        // 更新投注單狀態
        sportBet.setWinAmount(winAmount);
        sportBet.setStatus(SportBetStatus.SETTLED);
        sportBet.setSettledAt(LocalDateTime.now());
        sportBetRepository.save(sportBet);

        // 派彩（如果有贏）
        if (winAmount.compareTo(BigDecimal.ZERO) > 0) {
            User user = sportBet.getUser();
            BigDecimal balanceBefore = user.getSportBalance();
            BigDecimal balanceAfter = balanceBefore.add(winAmount);
            user.setSportBalance(balanceAfter);
            userRepository.save(user);

            SportTransaction transaction = SportTransaction.builder()
                    .merchant(sportBet.getMerchant())
                    .user(user)
                    .sportBet(sportBet)
                    .type(SportTransactionType.SPORT_WIN)
                    .amount(winAmount)
                    .balanceBefore(balanceBefore)
                    .balanceAfter(balanceAfter)
                    .description("體育派彩")
                    .build();
            sportTransactionRepository.save(transaction);
        }

        log.info("投注單 {} 結算完成，贏取金額: {}", betId, winAmount);
    }

    /**
     * 結算單腿（根據賽事結果）
     */
    @Transactional
    public void settleLeg(Long eventId, int homeScore, int awayScore,
                          Integer homeScoreHalf, Integer awayScoreHalf) {
        List<BetLeg> pendingLegs = betLegRepository.findPendingByEventId(eventId);

        for (BetLeg leg : pendingLegs) {
            SettlementResult result = calculateLegResult(leg, homeScore, awayScore,
                    homeScoreHalf, awayScoreHalf);
            BigDecimal resultFactor = getResultFactor(result);

            leg.setResult(result);
            leg.setResultFactor(resultFactor);
            betLegRepository.save(leg);

            log.info("投注腿 {} 結算完成，結果: {}, 係數: {}", leg.getId(), result, resultFactor);
        }
    }

    // ==================== 取消投注功能 ====================

    /**
     * 取消投注（僅限投注後指定時間內，且賽事尚未開始）
     *
     * @param merchant 商戶
     * @param user 用戶
     * @param request 取消請求
     * @return 取消響應
     */
    @Transactional
    public CancelBetResponse cancelBet(Merchant merchant, User user, CancelBetRequest request) {
        Long userId = user.getId();
        String lockKey = GlobeConstant.USER + GlobeConstant.SEMICOLON + merchant.getApiKey()
                + GlobeConstant.SEMICOLON + RedisConstant.PLACE_BET + "CANCEL:" + userId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLock.tryLockWithRetry(lockKey, lockValue, 30, 3);
        if (!locked) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "系統繁忙，請稍後再試");
        }

        try {
            // 查詢投注單
            SportBet sportBet = sportBetRepository.findByIdAndUserId(request.getBetId(), userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投注單不存在"));

            // 驗證是否可取消
            validateCancelAllowed(sportBet);

            // 使用悲觀鎖重新查詢用戶
            User lockedUser = userRepository.findByIdWithLock(merchant.getId(), userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用戶不存在"));

            // 退還金額
            BigDecimal refundAmount = sportBet.getStake();
            BigDecimal balanceBefore = lockedUser.getSportBalance();
            BigDecimal balanceAfter = balanceBefore.add(refundAmount);
            lockedUser.setSportBalance(balanceAfter);
            userRepository.save(lockedUser);

            // 更新投注單狀態
            sportBet.setStatus(SportBetStatus.CANCELLED);
            sportBet.setCancelledAt(LocalDateTime.now());
            sportBet.setCancelReason(request.getReason() != null ? request.getReason() : "用戶取消");
            sportBet.setValidBet(BigDecimal.ZERO);
            sportBetRepository.save(sportBet);

            // 更新所有投注腿為 VOID
            List<BetLeg> legs = betLegRepository.findByBetIdWithDetails(sportBet.getId());
            for (BetLeg leg : legs) {
                leg.setResult(SettlementResult.VOID);
                leg.setResultFactor(BigDecimal.ZERO);
            }
            betLegRepository.saveAll(legs);

            // 記錄交易流水
            SportTransaction transaction = SportTransaction.builder()
                    .merchant(merchant)
                    .user(lockedUser)
                    .sportBet(sportBet)
                    .type(SportTransactionType.SPORT_CANCEL)
                    .amount(refundAmount)
                    .balanceBefore(balanceBefore)
                    .balanceAfter(balanceAfter)
                    .description("投注取消退款 - " + (request.getReason() != null ? request.getReason() : "用戶取消"))
                    .build();
            sportTransactionRepository.save(transaction);

            log.info("投注單 {} 已取消，退還金額: {}", sportBet.getId(), refundAmount);

            return CancelBetResponse.builder()
                    .betId(sportBet.getId())
                    .refundAmount(refundAmount)
                    .balanceAfter(balanceAfter)
                    .cancelledAt(sportBet.getCancelledAt())
                    .reason(sportBet.getCancelReason())
                    .status(SportBetStatus.CANCELLED.name())
                    .build();

        } finally {
            redisLock.releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 驗證投注是否可取消
     */
    private void validateCancelAllowed(SportBet sportBet) {
        // 檢查狀態
        if (sportBet.getStatus() != SportBetStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只有待結算的投注可以取消");
        }

        // 檢查下注時間（僅允許下注後指定分鐘內取消）
        LocalDateTime now = LocalDateTime.now();
        Duration timeSincePlaced = Duration.between(sportBet.getPlacedAt(), now);
        if (timeSincePlaced.toMinutes() > CANCEL_ALLOWED_MINUTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "下注後超過 " + CANCEL_ALLOWED_MINUTES + " 分鐘，無法取消");
        }

        // 檢查所有賽事是否已開始
        List<BetLeg> legs = betLegRepository.findByBetIdWithDetails(sportBet.getId());
        for (BetLeg leg : legs) {
            if (leg.getEvent().getStartTime().isBefore(now)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "部分賽事已開始，無法取消");
            }
        }
    }

    // ==================== 提前兌現（Cashout）功能 ====================

    /**
     * 查詢提前兌現報價
     *
     * @param user 用戶
     * @param betId 投注單ID
     * @return 兌現報價
     */
    public CashoutQuoteResponse getCashoutQuote(User user, Long betId) {
        SportBet sportBet = sportBetRepository.findByIdAndUserId(betId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投注單不存在"));

        // 檢查是否可兌現
        CashoutValidationResult validation = validateCashoutAllowed(sportBet);

        if (!validation.isAllowed()) {
            return CashoutQuoteResponse.builder()
                    .betId(betId)
                    .cashoutAvailable(false)
                    .unavailableReason(validation.getReason())
                    .originalStake(sportBet.getStake())
                    .potentialWin(sportBet.getPotentialWin())
                    .build();
        }

        // 計算可兌現金額
        BigDecimal cashoutAmount = calculateCashoutAmount(sportBet);
        BigDecimal profitLoss = cashoutAmount.subtract(sportBet.getStake());
        BigDecimal cashoutPercentage = sportBet.getPotentialWin().compareTo(BigDecimal.ZERO) > 0
                ? cashoutAmount.divide(sportBet.getPotentialWin(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return CashoutQuoteResponse.builder()
                .betId(betId)
                .cashoutAvailable(true)
                .originalStake(sportBet.getStake())
                .potentialWin(sportBet.getPotentialWin())
                .cashoutAmount(cashoutAmount)
                .profitLoss(profitLoss)
                .cashoutPercentage(cashoutPercentage)
                .validForSeconds(CASHOUT_QUOTE_VALID_SECONDS)
                .build();
    }

    /**
     * 執行提前兌現
     *
     * @param merchant 商戶
     * @param user 用戶
     * @param request 兌現請求
     * @return 兌現響應
     */
    @Transactional
    public CashoutResponse cashout(Merchant merchant, User user, CashoutRequest request) {
        Long userId = user.getId();
        String lockKey = GlobeConstant.USER + GlobeConstant.SEMICOLON + merchant.getApiKey()
                + GlobeConstant.SEMICOLON + RedisConstant.PLACE_BET + "CASHOUT:" + userId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLock.tryLockWithRetry(lockKey, lockValue, 30, 3);
        if (!locked) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "系統繁忙，請稍後再試");
        }

        try {
            // 查詢投注單
            SportBet sportBet = sportBetRepository.findByIdAndUserId(request.getBetId(), userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投注單不存在"));

            // 驗證是否可兌現
            CashoutValidationResult validation = validateCashoutAllowed(sportBet);
            if (!validation.isAllowed()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validation.getReason());
            }

            // 計算兌現金額
            BigDecimal cashoutAmount = calculateCashoutAmount(sportBet);

            // 如果用戶提供了確認金額，驗證金額是否一致（允許一定誤差）
            if (request.getConfirmedAmount() != null) {
                BigDecimal diff = cashoutAmount.subtract(request.getConfirmedAmount()).abs();
                BigDecimal tolerance = cashoutAmount.multiply(new BigDecimal("0.01")); // 1% 容差
                if (diff.compareTo(tolerance) > 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "兌現金額已變更，請重新獲取報價");
                }
            }

            // 使用悲觀鎖重新查詢用戶
            User lockedUser = userRepository.findByIdWithLock(merchant.getId(), userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用戶不存在"));

            // 更新用戶餘額
            BigDecimal balanceBefore = lockedUser.getSportBalance();
            BigDecimal balanceAfter = balanceBefore.add(cashoutAmount);
            lockedUser.setSportBalance(balanceAfter);
            userRepository.save(lockedUser);

            // 更新投注單狀態
            sportBet.setStatus(SportBetStatus.CASHED_OUT);
            sportBet.setCashoutAmount(cashoutAmount);
            sportBet.setWinAmount(cashoutAmount);
            sportBet.setSettledAt(LocalDateTime.now());
            sportBetRepository.save(sportBet);

            // 更新所有投注腿為 VOID
            List<BetLeg> legs = betLegRepository.findByBetIdWithDetails(sportBet.getId());
            for (BetLeg leg : legs) {
                leg.setResult(SettlementResult.VOID);
                leg.setResultFactor(BigDecimal.ZERO);
            }
            betLegRepository.saveAll(legs);

            // 記錄交易流水
            SportTransaction transaction = SportTransaction.builder()
                    .merchant(merchant)
                    .user(lockedUser)
                    .sportBet(sportBet)
                    .type(SportTransactionType.SPORT_CASHOUT)
                    .amount(cashoutAmount)
                    .balanceBefore(balanceBefore)
                    .balanceAfter(balanceAfter)
                    .description("體育投注提前兌現")
                    .build();
            sportTransactionRepository.save(transaction);

            log.info("投注單 {} 提前兌現完成，兌現金額: {}", sportBet.getId(), cashoutAmount);

            return CashoutResponse.builder()
                    .betId(sportBet.getId())
                    .originalStake(sportBet.getStake())
                    .potentialWin(sportBet.getPotentialWin())
                    .cashoutAmount(cashoutAmount)
                    .profitLoss(cashoutAmount.subtract(sportBet.getStake()))
                    .balanceAfter(balanceAfter)
                    .cashedOutAt(sportBet.getSettledAt())
                    .status(SportBetStatus.CASHED_OUT.name())
                    .build();

        } finally {
            redisLock.releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 驗證投注是否可提前兌現
     */
    private CashoutValidationResult validateCashoutAllowed(SportBet sportBet) {
        // 檢查狀態
        if (sportBet.getStatus() != SportBetStatus.PENDING) {
            return new CashoutValidationResult(false, "只有待結算的投注可以提前兌現");
        }

        // 檢查是否有腿已經結算
        List<BetLeg> legs = betLegRepository.findByBetIdWithDetails(sportBet.getId());

        // 檢查是否有賽事已結束
        boolean hasCompletedEvent = legs.stream()
                .anyMatch(leg -> {
                    SportEvent event = leg.getEvent();
                    // 賽事狀態為已完成
                    return event.getSportEventStatus() == SportEventStatus.FINISHED;
                });

        if (hasCompletedEvent) {
            return new CashoutValidationResult(false, "部分賽事已結束，無法提前兌現");
        }

        // 檢查賽事狀態（賽事被取消的情況）
        boolean hasCancelledEvent = legs.stream()
                .anyMatch(leg -> leg.getEvent().getSportEventStatus() == SportEventStatus.CANCELLED ||
                                 leg.getEvent().getSportEventStatus() == SportEventStatus.POSTPONED);
        if (hasCancelledEvent) {
            return new CashoutValidationResult(false, "部分賽事已取消或延期，無法提前兌現");
        }

        return new CashoutValidationResult(true, null);
    }

    /**
     * 計算提前兌現金額
     *
     * 計算邏輯：
     * 1. 對於賽前投注：基礎金額 = 本金 * 當前賠率期望值
     * 2. 對於進行中的賽事：根據比分和剩餘時間調整
     * 3. 扣除手續費
     */
    private BigDecimal calculateCashoutAmount(SportBet sportBet) {
        List<BetLeg> legs = betLegRepository.findByBetIdWithDetails(sportBet.getId());
        BigDecimal stake = sportBet.getStake();

        if (sportBet.getBetType() == SportBetType.SINGLE) {
            return calculateSingleCashout(stake, legs.get(0));
        } else {
            return calculateParlayCashout(stake, legs);
        }
    }

    /**
     * 計算單注兌現金額
     */
    private BigDecimal calculateSingleCashout(BigDecimal stake, BetLeg leg) {
        SportEvent event = leg.getEvent();
        LocalDateTime now = LocalDateTime.now();

        BigDecimal cashoutBase;

        if (event.getStartTime().isAfter(now)) {
            // 賽前：返回本金的一定比例（考慮賠率變化風險）
            // 簡化計算：本金 * 0.9（扣除手續費）
            cashoutBase = stake.multiply(BigDecimal.ONE.subtract(CASHOUT_FEE_RATE));
        } else {
            // 進行中：根據當前狀況計算
            // 簡化計算：本金 * 當前勝率估算 * 賠率
            BigDecimal estimatedWinProbability = estimateWinProbability(leg, event);
            BigDecimal oddsDecimal = leg.getOddsDecimal();

            // 期望值 = 勝率 * 賠率
            BigDecimal expectedValue = estimatedWinProbability.multiply(oddsDecimal);
            cashoutBase = stake.multiply(expectedValue)
                    .multiply(BigDecimal.ONE.subtract(CASHOUT_FEE_RATE));
        }

        // 確保不超過預計最大贏取金額的 95%
        BigDecimal maxCashout = stake.multiply(leg.getOddsDecimal())
                .multiply(new BigDecimal("0.95"));

        // 確保至少返回本金的 10%（防止完全損失）
        BigDecimal minCashout = stake.multiply(new BigDecimal("0.10"));

        return cashoutBase.max(minCashout).min(maxCashout)
                .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 計算串關兌現金額
     */
    private BigDecimal calculateParlayCashout(BigDecimal stake, List<BetLeg> legs) {
        LocalDateTime now = LocalDateTime.now();

        // 計算各腿的有效賠率
        BigDecimal effectiveOdds = BigDecimal.ONE;
        int pendingLegs = 0;
        int favorableLegs = 0;

        for (BetLeg leg : legs) {
            SportEvent event = leg.getEvent();

            if (event.getStartTime().isAfter(now)) {
                // 未開始的賽事：使用原始賠率
                effectiveOdds = effectiveOdds.multiply(leg.getOddsDecimal());
                pendingLegs++;
            } else if (event.getSportEventStatus() == SportEventStatus.LIVE) {
                // 進行中的賽事：使用估算勝率
                BigDecimal estimatedWinProb = estimateWinProbability(leg, event);
                BigDecimal adjustedOdds = leg.getOddsDecimal().multiply(estimatedWinProb);
                effectiveOdds = effectiveOdds.multiply(adjustedOdds.max(new BigDecimal("0.1")));

                if (estimatedWinProb.compareTo(new BigDecimal("0.5")) > 0) {
                    favorableLegs++;
                }
            } else {
                // 已結束但未結算的賽事（不應該發生）
                effectiveOdds = effectiveOdds.multiply(leg.getOddsDecimal());
            }
        }

        // 基礎兌現金額
        BigDecimal cashoutBase = stake.multiply(effectiveOdds)
                .multiply(BigDecimal.ONE.subtract(CASHOUT_FEE_RATE));

        // 根據有利腿數調整
        if (favorableLegs > legs.size() / 2) {
            // 多數有利，提高兌現金額
            cashoutBase = cashoutBase.multiply(new BigDecimal("1.1"));
        }

        // 限制範圍
        BigDecimal potentialWin = stake.multiply(legs.stream()
                .map(BetLeg::getOddsDecimal)
                .reduce(BigDecimal.ONE, BigDecimal::multiply));
        BigDecimal maxCashout = potentialWin.multiply(new BigDecimal("0.95"));
        BigDecimal minCashout = stake.multiply(new BigDecimal("0.05")); // 串關最低返還 5%

        return cashoutBase.max(minCashout).min(maxCashout)
                .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 估算投注腿的勝率
     *
     * 注意：這是簡化版本，實際應用中應該使用更複雜的算法，
     * 考慮即時比分、剩餘時間、歷史數據等因素
     */
    private BigDecimal estimateWinProbability(BetLeg leg, SportEvent event) {
        // 獲取當前比分
        Integer homeScore = event.getHomeScore();
        Integer awayScore = event.getAwayScore();

        if (homeScore == null || awayScore == null) {
            // 如果沒有比分資訊，使用預設勝率
            return new BigDecimal("0.5");
        }

        int scoreDiff = homeScore - awayScore;
        BetLegSelection selection = leg.getSelection();
        String betTypeCode = leg.getBetTypeCode();
        BigDecimal handicap = leg.getHandicap() != null ? leg.getHandicap() : BigDecimal.ZERO;

        // 讓球盤
        if (betTypeCode.contains("AH") || betTypeCode.contains("SPREAD")) {
            BigDecimal adjustedDiff;
            if (selection == BetLegSelection.HOME) {
                adjustedDiff = new BigDecimal(scoreDiff).add(handicap);
            } else {
                adjustedDiff = new BigDecimal(-scoreDiff).subtract(handicap);
            }

            // 根據調整後的分差估算勝率
            if (adjustedDiff.compareTo(new BigDecimal("2")) >= 0) {
                return new BigDecimal("0.85");
            } else if (adjustedDiff.compareTo(BigDecimal.ONE) >= 0) {
                return new BigDecimal("0.70");
            } else if (adjustedDiff.compareTo(BigDecimal.ZERO) > 0) {
                return new BigDecimal("0.60");
            } else if (adjustedDiff.compareTo(BigDecimal.ZERO) == 0) {
                return new BigDecimal("0.50");
            } else if (adjustedDiff.compareTo(new BigDecimal("-1")) > 0) {
                return new BigDecimal("0.40");
            } else if (adjustedDiff.compareTo(new BigDecimal("-2")) > 0) {
                return new BigDecimal("0.30");
            } else {
                return new BigDecimal("0.15");
            }
        }

        // 大小盤
        if (betTypeCode.contains("OU") || betTypeCode.contains("TOTAL")) {
            int totalScore = homeScore + awayScore;
            BigDecimal diff;
            if (selection == BetLegSelection.OVER) {
                diff = new BigDecimal(totalScore).subtract(handicap);
            } else {
                diff = handicap.subtract(new BigDecimal(totalScore));
            }

            if (diff.compareTo(new BigDecimal("2")) >= 0) {
                return new BigDecimal("0.85");
            } else if (diff.compareTo(BigDecimal.ONE) >= 0) {
                return new BigDecimal("0.70");
            } else if (diff.compareTo(BigDecimal.ZERO) > 0) {
                return new BigDecimal("0.60");
            } else {
                return new BigDecimal("0.40");
            }
        }

        // 獨贏盤
        if (betTypeCode.equals("1X2") || betTypeCode.contains("ML")) {
            switch (selection) {
                case HOME:
                    if (scoreDiff > 0) return new BigDecimal("0.75");
                    if (scoreDiff == 0) return new BigDecimal("0.45");
                    return new BigDecimal("0.25");
                case AWAY:
                    if (scoreDiff < 0) return new BigDecimal("0.75");
                    if (scoreDiff == 0) return new BigDecimal("0.45");
                    return new BigDecimal("0.25");
                case DRAW:
                    if (scoreDiff == 0) return new BigDecimal("0.55");
                    return new BigDecimal("0.25");
                default:
                    return new BigDecimal("0.5");
            }
        }

        // 預設
        return new BigDecimal("0.5");
    }

    /**
     * Cashout 驗證結果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class CashoutValidationResult {
        private boolean allowed;
        private String reason;
    }

    // ==================== 私有方法 ====================

    /**
     * 驗證投注腿數
     */
    private void validateLegCount(SportBetType betType, int legCount) {
        if (betType == SportBetType.SINGLE && legCount != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "單注只能有一腿");
        }
        if (betType == SportBetType.PARLAY) {
            if (legCount < MIN_PARLAY_LEGS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "串關至少需要 " + MIN_PARLAY_LEGS + " 腿");
            }
            if (legCount > MAX_PARLAY_LEGS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "串關最多 " + MAX_PARLAY_LEGS + " 腿");
            }
        }
    }

    /**
     * 驗證投注金額
     */
    private void validateStake(BigDecimal stake) {
        if (stake.compareTo(MIN_STAKE) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "最小投注金額為 " + MIN_STAKE);
        }
        if (stake.compareTo(MAX_STAKE) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "最大投注金額為 " + MAX_STAKE);
        }
    }

    /**
     * 驗證並處理每一腿
     */
    private List<BetLegData> validateAndProcessLegs(List<SportBetRequest.BetLegRequest> legs,
                                                     SportBetType betType) {
        Set<Long> eventIds = new HashSet<>();
        List<BetLegData> legDataList = new ArrayList<>();

        for (SportBetRequest.BetLegRequest legReq : legs) {
            // 串關不能投注同一場賽事
            if (betType == SportBetType.PARLAY) {
                if (eventIds.contains(legReq.getEventId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "串關不能投注同一場賽事");
                }
                eventIds.add(legReq.getEventId());
            }

            // 查詢並驗證賽事
            SportEvent event = sportEventRepository.findByIdWithDetails(legReq.getEventId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "賽事不存在: " + legReq.getEventId()));

            if (event.getSportEventBettingStatus() != SportEventBettingStatus.OPEN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "賽事已停止投注: " + event.getHomeTeamName() + " vs " + event.getAwayTeamName());
            }

            if (event.getStartTime().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "賽事已開始: " + event.getHomeTeamName() + " vs " + event.getAwayTeamName());
            }

            // 查詢並驗證盤口
            MarketLine marketLine = marketLineRepository.findByIdWithDetails(legReq.getMarketLineId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "盤口不存在: " + legReq.getMarketLineId()));

            if (!marketLine.getEvent().getId().equals(event.getId())) {
                log.info("盤口 {} 賽事ID {} 與投注腿賽事ID {} 不匹配",
                        marketLine.getId(), marketLine.getEvent().getId(), event.getId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "盤口與賽事不匹配");
            }

            // 獲取選擇的賠率
            BetLegSelection selection = BetLegSelection.valueOf(legReq.getSelection());
            BigDecimal odds = getOddsForSelection(marketLine, selection, legReq.getCorrectScore());
            if (odds == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "無效的選擇: " + legReq.getSelection());
            }

            // 轉換為歐洲盤賠率
            BigDecimal oddsDecimal = convertToDecimalOdds(odds, marketLine.getOddsFormat().getCode());

            legDataList.add(BetLegData.builder()
                    .event(event)
                    .marketLine(marketLine)
                    .selection(selection)
                    .handicap(marketLine.getHandicap())
                    .odds(odds)
                    .oddsDecimal(oddsDecimal)
                    .betTypeCode(marketLine.getBetType().getCode())
                    .oddsFormatCode(marketLine.getOddsFormat().getCode())
                    .correctScore(legReq.getCorrectScore())
                    .build());
        }

        return legDataList;
    }

    /**
     * 根據選擇項獲取賠率
     */
    private BigDecimal getOddsForSelection(MarketLine ml, BetLegSelection selection, String correctScore) {
        return switch (selection) {
            case HOME -> ml.getHomeOdds();
            case AWAY -> ml.getAwayOdds();
            case DRAW -> ml.getDrawOdds();
            case OVER -> ml.getOverOdds();
            case UNDER -> ml.getUnderOdds();
            case YES -> ml.getYesOdds();
            case NO -> ml.getNoOdds();
            case ODD -> ml.getOddOdds();
            case EVEN -> ml.getEvenOdds();
        };
    }

    /**
     * 轉換為歐洲盤（Decimal）賠率
     */
    private BigDecimal convertToDecimalOdds(BigDecimal odds, String oddsFormat) {
        if (odds == null) return BigDecimal.ONE;

        return switch (oddsFormat) {
            case "EUROPEAN" -> odds;
            case "HONGKONG", "ASIAN", "INDIAN" -> odds.add(BigDecimal.ONE);
            case "MALAY" -> {
                if (odds.compareTo(BigDecimal.ZERO) >= 0) {
                    yield odds.add(BigDecimal.ONE);
                } else {
                    yield BigDecimal.ONE.subtract(BigDecimal.ONE.divide(odds.abs(), 4, RoundingMode.HALF_UP));
                }
            }
            case "INDO" -> {
                if (odds.compareTo(BigDecimal.ZERO) >= 0) {
                    yield odds.add(BigDecimal.ONE);
                } else {
                    yield BigDecimal.ONE.add(BigDecimal.ONE.divide(odds.abs(), 4, RoundingMode.HALF_UP));
                }
            }
            case "AMERICAN" -> {
                if (odds.compareTo(BigDecimal.ZERO) > 0) {
                    yield odds.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP).add(BigDecimal.ONE);
                } else {
                    yield new BigDecimal("100").divide(odds.abs(), 4, RoundingMode.HALF_UP).add(BigDecimal.ONE);
                }
            }
            default -> odds.add(BigDecimal.ONE);
        };
    }

    /**
     * 計算總賠率
     */
    private BigDecimal calculateTotalOdds(List<BetLegData> legDataList, SportBetType betType) {
        if (betType == SportBetType.SINGLE) {
            return legDataList.get(0).getOddsDecimal();
        }
        // 串關：所有腿的歐洲盤賠率相乘
        return legDataList.stream()
                .map(BetLegData::getOddsDecimal)
                .reduce(BigDecimal.ONE, BigDecimal::multiply)
                .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 計算預計贏取金額
     */
    private BigDecimal calculatePotentialWin(BigDecimal stake, BigDecimal totalOdds) {
        return stake.multiply(totalOdds).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 建立投注腿
     */
    private List<BetLeg> createBetLegs(SportBet sportBet, List<BetLegData> legDataList) {
        return legDataList.stream().map(data -> BetLeg.builder()
                .bet(sportBet)
                .event(data.getEvent())
                .marketLine(data.getMarketLine())
                .betTypeCode(data.getBetTypeCode())
                .oddsFormatCode(data.getOddsFormatCode())
                .selection(data.getSelection())
                .handicap(data.getHandicap())
                .odds(data.getOdds())
                .oddsDecimal(data.getOddsDecimal())
                .result(SettlementResult.PENDING)
                .resultFactor(BigDecimal.ONE)
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * 計算投注單贏取金額（含半贏半輸邏輯）
     */
    private BigDecimal calculateWinAmount(SportBet sportBet, List<BetLeg> legs) {
        BigDecimal stake = sportBet.getStake();

        if (sportBet.getBetType() == SportBetType.SINGLE) {
            BetLeg leg = legs.get(0);
            return calculateSingleWin(stake, leg);
        } else {
            // 串關計算
            return calculateParlayWin(stake, legs);
        }
    }

    /**
     * 計算單注贏取金額
     */
    private BigDecimal calculateSingleWin(BigDecimal stake, BetLeg leg) {
        SettlementResult result = leg.getResult();
        BigDecimal oddsDecimal = leg.getOddsDecimal();

        return switch (result) {
            case WIN -> stake.multiply(oddsDecimal).setScale(4, RoundingMode.HALF_UP);
            case HALF_WIN -> {
                // 半贏：(本金 + 本金 * (賠率-1) / 2)
                BigDecimal profit = stake.multiply(oddsDecimal.subtract(BigDecimal.ONE))
                        .divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
                yield stake.add(profit);
            }
            case PUSH, VOID -> stake; // 退還本金
            case HALF_LOSE -> stake.divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP); // 輸一半
            case LOSE -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 計算串關贏取金額
     */
    private BigDecimal calculateParlayWin(BigDecimal stake, List<BetLeg> legs) {
        // 檢查是否有任何一腿全輸
        boolean hasLose = legs.stream()
                .anyMatch(leg -> leg.getResult() == SettlementResult.LOSE);
        if (hasLose) {
            return BigDecimal.ZERO;
        }

        // 計算有效賠率（考慮半贏半輸和退款）
        BigDecimal effectiveOdds = BigDecimal.ONE;
        for (BetLeg leg : legs) {
            BigDecimal legOdds = calculateEffectiveLegOdds(leg);
            effectiveOdds = effectiveOdds.multiply(legOdds);
        }

        return stake.multiply(effectiveOdds).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 計算單腿的有效賠率（用於串關計算）
     */
    private BigDecimal calculateEffectiveLegOdds(BetLeg leg) {
        BigDecimal oddsDecimal = leg.getOddsDecimal();
        SettlementResult result = leg.getResult();

        return switch (result) {
            case WIN -> oddsDecimal;
            case HALF_WIN -> {
                // 半贏：1 + (賠率-1) / 2
                BigDecimal halfProfit = oddsDecimal.subtract(BigDecimal.ONE)
                        .divide(new BigDecimal("2"), 4, RoundingMode.HALF_UP);
                yield BigDecimal.ONE.add(halfProfit);
            }
            case PUSH, VOID -> BigDecimal.ONE; // 當作賠率1
            case HALF_LOSE -> new BigDecimal("0.5"); // 輸一半
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 計算單腿結果（含讓球、大小邏輯）
     */
    private SettlementResult calculateLegResult(BetLeg leg, int homeScore, int awayScore,
                                                Integer homeScoreHalf, Integer awayScoreHalf) {
        String betTypeCode = leg.getBetTypeCode();
        BetLegSelection selection = leg.getSelection();
        BigDecimal handicap = leg.getHandicap() != null ? leg.getHandicap() : BigDecimal.ZERO;

        // 判斷是否為半場玩法
        boolean isHalfTime = betTypeCode.contains("HALF");
        int hScore = isHalfTime && homeScoreHalf != null ? homeScoreHalf : homeScore;
        int aScore = isHalfTime && awayScoreHalf != null ? awayScoreHalf : awayScore;
        int totalScore = hScore + aScore;
        int scoreDiff = hScore - aScore;

        // 讓球盤
        if (betTypeCode.contains("AH") || betTypeCode.contains("SPREAD")) {
            return calculateHandicapResult(selection, scoreDiff, handicap);
        }

        // 大小盤
        if (betTypeCode.contains("OU") || betTypeCode.contains("TOTAL")) {
            return calculateOverUnderResult(selection, totalScore, handicap);
        }

        // 獨贏盤（1X2）
        if (betTypeCode.equals("1X2") || betTypeCode.equals("1X2_HALF") || betTypeCode.contains("ML")) {
            return calculateMoneylineResult(selection, scoreDiff);
        }

        // 單雙盤
        if (betTypeCode.equals("OE")) {
            return calculateOddEvenResult(selection, totalScore);
        }

        // 兩隊都進球
        if (betTypeCode.equals("BTTS")) {
            boolean bothScored = hScore > 0 && aScore > 0;
            if (selection == BetLegSelection.YES) {
                return bothScored ? SettlementResult.WIN : SettlementResult.LOSE;
            } else {
                return bothScored ? SettlementResult.LOSE : SettlementResult.WIN;
            }
        }

        return SettlementResult.PENDING;
    }

    /**
     * 計算讓球盤結果
     */
    private SettlementResult calculateHandicapResult(BetLegSelection selection, int scoreDiff, BigDecimal handicap) {
        // 計算調整後的分差
        BigDecimal adjustedDiff;
        if (selection == BetLegSelection.HOME) {
            adjustedDiff = new BigDecimal(scoreDiff).add(handicap);
        } else {
            adjustedDiff = new BigDecimal(-scoreDiff).subtract(handicap);
        }

        return evaluateHandicapDiff(adjustedDiff);
    }

    /**
     * 計算大小盤結果
     */
    private SettlementResult calculateOverUnderResult(BetLegSelection selection, int totalScore, BigDecimal line) {
        BigDecimal total = new BigDecimal(totalScore);
        BigDecimal diff;

        if (selection == BetLegSelection.OVER) {
            diff = total.subtract(line);
        } else {
            diff = line.subtract(total);
        }

        return evaluateHandicapDiff(diff);
    }

    /**
     * 評估讓球/大小差值結果（支援四分盤）
     */
    private SettlementResult evaluateHandicapDiff(BigDecimal diff) {
        int cmp = diff.compareTo(BigDecimal.ZERO);

        if (cmp > 0) {
            // 大於0.25為全贏，0到0.25為半贏
            if (diff.compareTo(new BigDecimal("0.25")) >= 0) {
                return SettlementResult.WIN;
            } else {
                return SettlementResult.HALF_WIN;
            }
        } else if (cmp == 0) {
            return SettlementResult.PUSH;
        } else {
            // 小於-0.25為全輸，-0.25到0為半輸
            if (diff.compareTo(new BigDecimal("-0.25")) <= 0) {
                return SettlementResult.LOSE;
            } else {
                return SettlementResult.HALF_LOSE;
            }
        }
    }

    /**
     * 計算獨贏盤結果
     */
    private SettlementResult calculateMoneylineResult(BetLegSelection selection, int scoreDiff) {
        return switch (selection) {
            case HOME -> scoreDiff > 0 ? SettlementResult.WIN : SettlementResult.LOSE;
            case AWAY -> scoreDiff < 0 ? SettlementResult.WIN : SettlementResult.LOSE;
            case DRAW -> scoreDiff == 0 ? SettlementResult.WIN : SettlementResult.LOSE;
            default -> SettlementResult.PENDING;
        };
    }

    /**
     * 計算單雙盤結果
     */
    private SettlementResult calculateOddEvenResult(BetLegSelection selection, int totalScore) {
        boolean isOdd = totalScore % 2 != 0;
        if (selection == BetLegSelection.ODD) {
            return isOdd ? SettlementResult.WIN : SettlementResult.LOSE;
        } else {
            return isOdd ? SettlementResult.LOSE : SettlementResult.WIN;
        }
    }

    /**
     * 獲取結算係數
     */
    private BigDecimal getResultFactor(SettlementResult result) {
        return switch (result) {
            case WIN -> BigDecimal.ONE;
            case HALF_WIN -> new BigDecimal("0.5");
            case PUSH, VOID -> BigDecimal.ZERO;
            case HALF_LOSE -> new BigDecimal("-0.5");
            case LOSE -> new BigDecimal("-1.0");
            default -> BigDecimal.ONE;
        };
    }

    /**
     * 發送投注消息
     */
    private void sendBetMessage(SportBet sportBet, SportTransaction transaction) {
        try {
            SportTransactionMessage message = SportTransactionMessage.builder()
                    .sportBetId(sportBet.getId())
                    .userId(sportBet.getUser().getId())
                    .merchantId(sportBet.getMerchant().getId())
                    .type(transaction.getType().name())
                    .amount(transaction.getAmount())
                    .balanceAfter(transaction.getBalanceAfter())
                    .timestamp(LocalDateTime.now())
                    .build();
            messageProducerService.sendSportTransactionMessage(message);
        } catch (Exception e) {
            log.error("發送投注消息失敗", e);
        }
    }

    /**
     * 建立投注響應
     */
    private SportBetResponse buildBetResponse(SportBet sportBet, List<BetLeg> legs, BigDecimal balanceAfter) {
        List<SportBetResponse.BetLegResponse> legResponses = legs.stream()
                .map(this::buildLegResponse)
                .collect(Collectors.toList());

        return SportBetResponse.builder()
                .betId(sportBet.getId())
                .betType(sportBet.getBetType().name())
                .stake(sportBet.getStake())
                .totalOdds(sportBet.getTotalOdds())
                .potentialWin(sportBet.getPotentialWin())
                .status(sportBet.getStatus().name())
                .placedAt(sportBet.getPlacedAt())
                .balanceAfter(balanceAfter)
                .legs(legResponses)
                .build();
    }

    /**
     * 建立投注記錄響應
     */
    private SportBetRecordResponse buildBetRecordResponse(SportBet sportBet) {
        List<BetLeg> legs = betLegRepository.findByBetIdWithDetails(sportBet.getId());
        List<SportBetResponse.BetLegResponse> legResponses = legs.stream()
                .map(this::buildLegResponse)
                .collect(Collectors.toList());

        return SportBetRecordResponse.builder()
                .betId(sportBet.getId())
                .betType(sportBet.getBetType().name())
                .stake(sportBet.getStake())
                .totalOdds(sportBet.getTotalOdds())
                .potentialWin(sportBet.getPotentialWin())
                .winAmount(sportBet.getWinAmount())
                .validBet(sportBet.getValidBet())
                .status(sportBet.getStatus().name())
                .placedAt(sportBet.getPlacedAt())
                .settledAt(sportBet.getSettledAt())
                .legs(legResponses)
                .build();
    }

    /**
     * 建立投注腿響應
     */
    private SportBetResponse.BetLegResponse buildLegResponse(BetLeg leg) {
        SportEvent event = leg.getEvent();
        MarketLine ml = leg.getMarketLine();

        return SportBetResponse.BetLegResponse.builder()
                .legId(leg.getId())
                .eventId(event.getId())
                .eventName(event.getHomeTeamName() + " vs " + event.getAwayTeamName())
                .leagueName(event.getLeague() != null ? event.getLeague().getName() : null)
                .sportTypeName(event.getSportType() != null ? event.getSportType().getName() : null)
                .startTime(event.getStartTime())
                .marketLineId(ml.getId())
                .betTypeCode(leg.getBetTypeCode())
                .betTypeName(ml.getBetType() != null ? ml.getBetType().getName() : null)
                .oddsFormatCode(leg.getOddsFormatCode())
                .selection(leg.getSelection().name())
                .selectionDisplay(getSelectionDisplay(leg.getSelection(), event))
                .handicap(leg.getHandicap())
                .odds(leg.getOdds())
                .oddsDecimal(leg.getOddsDecimal())
                .result(leg.getResult() != null ? leg.getResult().name() : null)
                .build();
    }

    /**
     * 獲取選擇項顯示文字
     */
    private String getSelectionDisplay(BetLegSelection selection, SportEvent event) {
        return switch (selection) {
            case HOME -> event.getHomeTeamName();
            case AWAY -> event.getAwayTeamName();
            case DRAW -> "平局";
            case OVER -> "大";
            case UNDER -> "小";
            case YES -> "是";
            case NO -> "否";
            case ODD -> "單";
            case EVEN -> "雙";
        };
    }

    /**
     * 投注腿資料（內部使用）
     */
    @lombok.Builder
    @lombok.Data
    private static class BetLegData {
        private SportEvent event;
        private MarketLine marketLine;
        private BetLegSelection selection;
        private BigDecimal handicap;
        private BigDecimal odds;
        private BigDecimal oddsDecimal;
        private String betTypeCode;
        private String oddsFormatCode;
        private String correctScore;
    }
}
