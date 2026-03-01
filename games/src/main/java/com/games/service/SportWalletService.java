package com.games.service;

import com.games.constant.GlobeConstant;
import com.games.constant.RedisConstant;
import com.games.dto.SportTransactionMessage;
import com.games.entity.Merchant;
import com.games.entity.SportBet;
import com.games.entity.User;
import com.games.enums.SportTransactionType;
import com.games.lock.RedisLock;
import com.games.repository.UserRepository;
import com.games.rocketmq.producer.MessageProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SportWalletService {

    private final UserRepository userRepository;
    private final MessageProducerService messageProducerService;
    private final RedisLock redisLock;

    public void createSportTransaction(User user, Merchant merchant,
                                       SportTransactionType type, BigDecimal amount, BigDecimal balanceBefore,
                                       BigDecimal balanceAfter, String description, SportBet sportBet) {
        SportTransactionMessage message = SportTransactionMessage.builder()
                .sportBetId(sportBet != null ? sportBet.getId() : null)
                .userId(user.getId())
                .merchantId(merchant.getId())
                .type(type.name())
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .description(description)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        messageProducerService.sendSportTransactionMessage(message);
        log.debug("Sport transaction message sent for user: {}, type: {}", user.getId(), type);
    }

    @Transactional
    public User deposit(Merchant merchant, User user, BigDecimal amount) {
        final Long userId = user.getId();  // 保存到 final 变量
        log.info("Sport Deposit new user: {}, amount:{}", userId, amount);

        String lockKey = GlobeConstant.USER + GlobeConstant.SEMICOLON + merchant.getApiKey()
                + GlobeConstant.SEMICOLON + RedisConstant.DEPOSIT + userId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLock.tryLockWithRetry(lockKey,
                lockValue, 30, 3);

        if (!locked){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not acquire lock" + lockKey);
        }

        try {
            // 使用悲观锁重新查询用户，确保数据库层面的并发安全
            User lockedUser = userRepository.findByIdWithLock(merchant.getId(), userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            BigDecimal balanceBefore = lockedUser.getSportBalance();
            BigDecimal newBalance = balanceBefore.add(amount);

            lockedUser.setGameBalance(newBalance);
            user = userRepository.save(lockedUser);

            createSportTransaction(user, merchant, SportTransactionType.SPORT_DEPOSIT, amount, balanceBefore, newBalance,
                    "Sport deposit to wallet", null);
        } finally {
            redisLock.releaseLock(lockKey, lockValue);
        }
        return user;
    }

    @Transactional
    public User withdrawAll(Merchant merchant, User user) {
        final Long userId = user.getId();  // 保存到 final 变量

        String lockKey = GlobeConstant.USER + GlobeConstant.SEMICOLON + merchant.getApiKey()
                + GlobeConstant.SEMICOLON + RedisConstant.WITHDRAW + userId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLock.tryLockWithRetry(lockKey,
                lockValue, 30, 3);

        if (!locked){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not acquire lock" + lockKey);
        }

        try {
            // 使用悲观锁重新查询用户，确保数据库层面的并发安全
            User lockedUser = userRepository.findByIdWithLock(merchant.getId(), userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            BigDecimal balanceBefore = lockedUser.getSportBalance();

            if (balanceBefore.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("No balance to withdraw");
            }

            BigDecimal withdrawAmount = balanceBefore;
            lockedUser.setSportBalance(BigDecimal.ZERO);
            user = userRepository.save(lockedUser);

            createSportTransaction(user, merchant, SportTransactionType.SPORT_WITHDRAW, withdrawAmount, balanceBefore,
                    BigDecimal.ZERO, "Sport withdraw all balance", null);
        } finally {
            redisLock.releaseLock(lockKey, lockValue);
        }
        return user;
    }
}
