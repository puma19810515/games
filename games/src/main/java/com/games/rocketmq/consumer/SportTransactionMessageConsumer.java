package com.games.rocketmq.consumer;

import com.games.constant.RocketMQConstant;
import com.games.dto.SportTransactionMessage;
import com.games.entity.*;
import com.games.enums.SportTransactionType;
import com.games.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = RocketMQConstant.SPORT_TRANSACTION_TOPIC,
        consumerGroup = "${rocketmq.consumer.sport-transaction-group}",
        selectorExpression = RocketMQConstant.SPORT_TRANSACTION_TAG
)
public class SportTransactionMessageConsumer implements RocketMQListener<SportTransactionMessage> {

    private static final String IDEMPOTENT_KEY_PREFIX = "sport:transaction:idempotent:";
    private static final Duration IDEMPOTENT_EXPIRE = Duration.ofHours(24);

    private final MerchantRepository merchantRepository;
    private final SportTransactionRepository sportTransactionRepository;
    private final UserRepository userRepository;
    private final SportBetRepository sportBetRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public void onMessage(SportTransactionMessage message) {
        // 生成唯一的冪等鍵
        String idempotentKey = buildIdempotentKey(message);

        // 冪等性檢查：如果已處理過，直接返回
        Boolean isNew = stringRedisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", IDEMPOTENT_EXPIRE);

        if (Boolean.FALSE.equals(isNew)) {
            log.warn("Duplicate sport transaction message detected, skipping: userId={}, type={}, sportBetId={}",
                    message.getUserId(), message.getType(), message.getSportBetId());
            return;
        }

        try {
            log.info("Processing sport transaction message: merchantId={}, userId={}, type={}, amount={}",
                    message.getMerchantId(), message.getUserId(), message.getType(), message.getAmount());

            User user = userRepository.findById(message.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + message.getUserId()));

            Merchant merchant = merchantRepository.findById(message.getMerchantId())
                    .orElseThrow(() -> new RuntimeException("Merchant not found: " + message.getMerchantId()));

            SportBet sportBet = null;
            if (message.getSportBetId() != null) {
                sportBet = sportBetRepository.findById(message.getSportBetId()).orElse(null);
            }

            SportTransaction sportTransaction = SportTransaction.builder()
                    .merchant(merchant)
                    .user(user)
                    .type(SportTransactionType.valueOf(message.getType()))
                    .amount(message.getAmount())
                    .balanceBefore(message.getBalanceBefore())
                    .balanceAfter(message.getBalanceAfter())
                    .description(message.getDescription())
                    .sportBet(sportBet)
                    .build();

            sportTransactionRepository.save(sportTransaction);

            log.info("Sport transaction message processed successfully: transactionId={}",
                    sportTransaction.getId());
        } catch (Exception e) {
            // 處理失敗時刪除冪等鍵，允許重試
            stringRedisTemplate.delete(idempotentKey);
            log.error("Failed to process sport transaction message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process sport transaction message", e);
        }
    }

    /**
     * 建立冪等鍵
     * 使用 userId + type + sportBetId + timestamp 作為唯一識別
     */
    private String buildIdempotentKey(SportTransactionMessage message) {
        return IDEMPOTENT_KEY_PREFIX +
                message.getUserId() + ":" +
                message.getType() + ":" +
                (message.getSportBetId() != null ? message.getSportBetId() : "null") + ":" +
                (message.getTimestamp() != null ? message.getTimestamp().toString() : System.currentTimeMillis());
    }
}
