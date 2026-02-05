package com.games.rocketmq.consumer;

import com.games.config.SnowflakeIdGenerator;
import com.games.constant.RocketMQConstant;
import com.games.dto.TransactionMessage;
import com.games.entity.Bet;
import com.games.entity.Transaction;
import com.games.entity.User;
import com.games.repository.BetRepository;
import com.games.repository.TransactionRepository;
import com.games.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = RocketMQConstant.TRANSACTION_TOPIC,
        consumerGroup = "${rocketmq.consumer.group}",
        selectorExpression = RocketMQConstant.TRANSACTION_TAG
)
public class TransactionMessageConsumer implements RocketMQListener<TransactionMessage> {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BetRepository betRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public void onMessage(TransactionMessage message) {
        try {
            log.info("Processing transaction message: userId={}, type={}, amount={}",
                    message.getUserId(), message.getType(), message.getAmount());

            User user = userRepository.findById(message.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + message.getUserId()));

            Bet bet = null;
            if (message.getBetId() != null) {
                bet = betRepository.findById(message.getBetId()).orElse(null);
            }

            Transaction transaction = new Transaction();
            transaction.setId(idGenerator.nextId());
            transaction.setUser(user);
            transaction.setType(message.getType());
            transaction.setAmount(message.getAmount());
            transaction.setBalanceBefore(message.getBalanceBefore());
            transaction.setBalanceAfter(message.getBalanceAfter());
            transaction.setDescription(message.getDescription());
            transaction.setBet(bet);

            transactionRepository.save(transaction);

            log.info("Transaction message processed successfully: transactionId={}", transaction.getId());
        } catch (Exception e) {
            log.error("Failed to process transaction message: {}", e.getMessage(), e);
            // 抛出异常会触发 RocketMQ 重试机制
            throw new RuntimeException("Failed to process transaction message", e);
        }
    }
}
