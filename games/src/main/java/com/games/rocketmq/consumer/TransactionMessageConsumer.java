package com.games.rocketmq.consumer;

import com.games.constant.RocketMQConstant;
import com.games.dto.TransactionMessage;
import com.games.entity.Bet;
import com.games.entity.Merchant;
import com.games.entity.Transaction;
import com.games.entity.User;
import com.games.repository.BetRepository;
import com.games.repository.MerchantRepository;
import com.games.repository.TransactionRepository;
import com.games.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = RocketMQConstant.TRANSACTION_TOPIC,
        consumerGroup = "${rocketmq.consumer.transaction-group}",
        selectorExpression = RocketMQConstant.TRANSACTION_TAG
)
public class TransactionMessageConsumer implements RocketMQListener<TransactionMessage> {

    private static final String IDEMPOTENT_KEY_PREFIX = "transaction:idempotent:";
    private static final Duration IDEMPOTENT_EXPIRE = Duration.ofHours(24);

    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BetRepository betRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(TransactionMessage message) {

        // 生成唯一的冪等鍵
        String idempotentKey = buildIdempotentKey(message);

        // 冪等性檢查：如果已處理過，直接返回
        Boolean isNew = stringRedisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", IDEMPOTENT_EXPIRE);

        if (Boolean.FALSE.equals(isNew)) {
            log.warn("Duplicate transaction message detected, skipping: userId={}, type={}, sportBetId={}",
                    message.getUserId(), message.getType(), message.getBetId());
            return;
        }
        try {
            log.info("Processing transaction message: merchantId={}, userId={}, type={}, amount={}",
                    message.getMerchantId(), message.getUserId(), message.getType(), message.getAmount());

            User user = userRepository.findById(message.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + message.getUserId()));

            Merchant merchant = merchantRepository.findById(message.getMerchantId())
                    .orElseThrow(() -> new RuntimeException("Merchant not found: " + message.getMerchantId()));

            Bet bet = null;
            if (message.getBetId() != null) {
                bet = betRepository.findById(message.getBetId()).orElse(null);
            }

            Transaction transaction = Transaction.builder()
                    .merchant(merchant)
                    .type(message.getType())
                    .amount(message.getAmount())
                    .bet(bet)
                    .balanceAfter(message.getBalanceAfter())
                    .balanceAfter(message.getBalanceAfter())
                    .description(message.getDescription())
                    .user(user)
                    .amount(message.getAmount())
                    .build();

            transactionRepository.save(transaction);

            log.info("Transaction message processed successfully: transactionId={}", transaction.getId());
        } catch (Exception e) {
            log.error("Failed to process transaction message: {}", e.getMessage(), e);
            // 抛出异常会触发 RocketMQ 重试机制
            throw new RuntimeException("Failed to process transaction message", e);
        }
    }

    /**
     * 建立冪等鍵
     * 使用 userId + type + betId + timestamp 作為唯一識別
     */
    private String buildIdempotentKey(TransactionMessage message) {
        return IDEMPOTENT_KEY_PREFIX +
                message.getUserId() + ":" +
                message.getType() + ":" +
                (message.getBetId() != null ? message.getBetId() : "null") + ":" +
                (message.getTimestamp() != null ? message.getTimestamp().toString() : System.currentTimeMillis());
    }
}
