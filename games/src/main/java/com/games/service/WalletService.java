package com.games.service;

import com.games.constant.GlobeConstant;
import com.games.constant.RedisConstant;
import com.games.dto.TransactionMessage;
import com.games.entity.Bet;
import com.games.entity.User;
import com.games.enums.TransactionType;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final UserRepository userRepository;
    private final MessageProducerService messageProducerService;
    private final RedisLock redisLock;

    /**
     * 扣除余额
     * 注意：此方法应该在已有事务中调用，不开启新事务
     * 传入的 user 对象应该已经通过悲观锁获取
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void deductBalance(User user, BigDecimal amount, Bet bet, String description) {
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal newBalance = balanceBefore.subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        user.setBalance(newBalance);
        userRepository.save(user);

        createTransaction(user, TransactionType.BET, amount, balanceBefore, newBalance, description, bet);
    }

    /**
     * 增加余额
     * 注意：此方法应该在已有事务中调用，不开启新事务
     * 传入的 user 对象应该已经通过悲观锁获取
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void addBalance(User user, BigDecimal amount, Bet bet, String description) {
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal newBalance = balanceBefore.add(amount);

        user.setBalance(newBalance);
        userRepository.save(user);

        createTransaction(user, TransactionType.WIN, amount, balanceBefore, newBalance, description, bet);
    }

    @Transactional
    public User deposit(User user, BigDecimal amount) {
        final Long userId = user.getId();  // 保存到 final 变量
        log.info("Deposit new user: {}, amount:{}", userId, amount);

        String lockKey = GlobeConstant.USER + GlobeConstant.SEMICOLON
                + RedisConstant.DEPOSIT + userId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLock.tryLockWithRetry(lockKey,
                lockValue, 30, 3);

        if (!locked){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not acquire lock" + lockKey);
        }

        try {
            // 使用悲观锁重新查询用户，确保数据库层面的并发安全
            User lockedUser = userRepository.findByIdWithLock(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            BigDecimal balanceBefore = lockedUser.getBalance();
            BigDecimal newBalance = balanceBefore.add(amount);

            lockedUser.setBalance(newBalance);
            user = userRepository.save(lockedUser);

            createTransaction(user, TransactionType.DEPOSIT, amount, balanceBefore, newBalance,
                    "Deposit to wallet", null);
        } finally {
            redisLock.releaseLock(lockKey, lockValue);
        }
        return user;
    }

    @Transactional
    public User withdrawAll(User user) {
        final Long userId = user.getId();  // 保存到 final 变量

        String lockKey = GlobeConstant.USER + GlobeConstant.SEMICOLON
                + RedisConstant.WITHDRAW + userId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisLock.tryLockWithRetry(lockKey,
                lockValue, 30, 3);

        if (!locked){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not acquire lock" + lockKey);
        }

        try {
            // 使用悲观锁重新查询用户，确保数据库层面的并发安全
            User lockedUser = userRepository.findByIdWithLock(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            BigDecimal balanceBefore = lockedUser.getBalance();

            if (balanceBefore.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("No balance to withdraw");
            }

            BigDecimal withdrawAmount = balanceBefore;
            lockedUser.setBalance(BigDecimal.ZERO);
            user = userRepository.save(lockedUser);

            createTransaction(user, TransactionType.WITHDRAW, withdrawAmount, balanceBefore,
                    BigDecimal.ZERO, "Withdraw all balance", null);
        } finally {
            redisLock.releaseLock(lockKey, lockValue);
        }
        return user;
    }

    public void createTransaction(User user, TransactionType type, BigDecimal amount,
                                   BigDecimal balanceBefore, BigDecimal balanceAfter,
                                   String description, Bet bet) {
        // 使用异步消息记录交易，提高性能
        TransactionMessage message = new TransactionMessage(
                user.getId(),
                type,
                amount,
                balanceBefore,
                balanceAfter,
                description,
                bet != null ? bet.getId() : null
        );
        messageProducerService.sendTransactionMessage(message);
        log.debug("Transaction message sent for user: {}, type: {}", user.getId(), type);
    }
}
