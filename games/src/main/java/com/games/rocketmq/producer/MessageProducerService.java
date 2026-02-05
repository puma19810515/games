package com.games.rocketmq.producer;

import com.games.constant.RocketMQConstant;
import com.games.dto.RtpUpdateMessage;
import com.games.dto.TransactionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducerService {

    private final RocketMQTemplate rocketMQTemplate;

    public void sendTransactionMessage(final TransactionMessage message) {
        try {
            String destination = RocketMQConstant.TRANSACTION_TOPIC + ":" + RocketMQConstant.TRANSACTION_TAG;
            rocketMQTemplate.asyncSend(destination, MessageBuilder.withPayload(message).build(),
                    new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            log.info("Transaction message sent successfully: userId={}, type={}, amount={}",
                                    message.getUserId(), message.getType(), message.getAmount());
                        }

                        @Override
                        public void onException(Throwable e) {
                            log.error("Failed to send transaction message: {}", e.getMessage(), e);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to send transaction message: {}", e.getMessage(), e);
        }
    }

    public void sendRtpUpdateMessage(final RtpUpdateMessage message) {
        try {
            String destination = RocketMQConstant.RTP_UPDATE_TOPIC + ":" + RocketMQConstant.RTP_TAG;
            rocketMQTemplate.asyncSend(destination, MessageBuilder.withPayload(message).build(),
                    new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            log.debug("RTP update message sent successfully: betAmount={}, winAmount={}",
                                    message.getBetAmount(), message.getWinAmount());
                        }

                        @Override
                        public void onException(Throwable e) {
                            log.error("Failed to send RTP update message: {}", e.getMessage(), e);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to send RTP update message: {}", e.getMessage(), e);
        }
    }
}
