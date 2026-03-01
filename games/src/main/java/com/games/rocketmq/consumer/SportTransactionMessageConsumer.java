package com.games.rocketmq.consumer;

import com.games.config.SnowflakeIdGenerator;
import com.games.constant.RocketMQConstant;
import com.games.dto.SportTransactionMessage;
import com.games.entity.*;
import com.games.enums.SportTransactionType;
import com.games.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = RocketMQConstant.SPORT_TRANSACTION_TOPIC,
        consumerGroup = "${rocketmq.consumer.sport-transaction-group}",
        selectorExpression = RocketMQConstant.SPORT_TRANSACTION_TAG
)
public class SportTransactionMessageConsumer implements RocketMQListener<SportTransactionMessage> {

    private final MerchantRepository merchantRepository;
    private final SportTransactionRepository sportTransactionRepository;
    private final UserRepository userRepository;
    private final SportBetRepository sportBetRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public void onMessage(SportTransactionMessage message) {
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

            SportTransaction sportTransaction = new SportTransaction();
            sportTransaction.setId(idGenerator.nextId());
            sportTransaction.setMerchant(merchant);
            sportTransaction.setUser(user);
            sportTransaction.setType(SportTransactionType.valueOf(message.getType()));
            sportTransaction.setAmount(message.getAmount());
            sportTransaction.setBalanceBefore(message.getBalanceBefore());
            sportTransaction.setBalanceAfter(message.getBalanceAfter());
            sportTransaction.setDescription(message.getDescription());
            sportTransaction.setSportBet(sportBet);

            sportTransactionRepository.save(sportTransaction);

            log.info("Sport transaction message processed successfully: sport " +
                    "transactionId={}", sportTransaction.getId());
        } catch (Exception e) {
            log.error("Failed to process sport transaction message: {}", e.getMessage(), e);
            // 抛出异常会触发 RocketMQ 重试机制
            throw new RuntimeException("Failed to process sport transaction message", e);
        }
    }
}
