package com.games.rocketmq.producer;

import com.games.constant.RocketMQConstant;
import com.games.dto.RtpUpdateMessage;
import com.games.dto.SportTransactionMessage;
import com.games.dto.TransactionMessage;
import com.games.enums.SportTransactionType;
import com.games.enums.TransactionType;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageProducerServiceTest {

    @Mock
    private RocketMQTemplate gamesTemplate;

    @Mock
    private RocketMQTemplate sportTemplate;

    @Captor
    @SuppressWarnings("rawtypes")
    private ArgumentCaptor<Message> messageCaptor;

    private MessageProducerService producerService;

    @BeforeEach
    void setUp() {
        producerService = new MessageProducerService(gamesTemplate, sportTemplate);
    }

    // ── sendTransactionMessage ──────────────────────────────────────────────

    @Test
    void sendTransactionMessage_shouldSendToCorrectDestination() {
        TransactionMessage msg = new TransactionMessage(
                1L, 1L, TransactionType.REGISTER,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("50.00"),
                "Initial balance", null);

        producerService.sendTransactionMessage(msg);

        String expected = RocketMQConstant.TRANSACTION_TOPIC + ":" + RocketMQConstant.TRANSACTION_TAG;
        verify(gamesTemplate).asyncSend(eq(expected), messageCaptor.capture(), any(SendCallback.class));
        assertThat(messageCaptor.getValue().getPayload()).isEqualTo(msg);
    }

    @Test
    void sendTransactionMessage_shouldNotPropagateExceptionWhenTemplateFails() {
        TransactionMessage msg = new TransactionMessage(
                1L, 1L, TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                "Deposit", null);

        doThrow(new RuntimeException("broker unavailable"))
                .when(gamesTemplate).asyncSend(any(), any(Message.class), any(SendCallback.class));

        assertThatNoException().isThrownBy(() -> producerService.sendTransactionMessage(msg));
    }

    // ── sendRtpUpdateMessage ────────────────────────────────────────────────

    @Test
    void sendRtpUpdateMessage_shouldSendToCorrectDestination() {
        RtpUpdateMessage msg = new RtpUpdateMessage(
                new BigDecimal("200.00"),
                new BigDecimal("150.00"),
                "SLOT_01");

        producerService.sendRtpUpdateMessage(msg);

        String expected = RocketMQConstant.RTP_UPDATE_TOPIC + ":" + RocketMQConstant.RTP_TAG;
        verify(gamesTemplate).asyncSend(eq(expected), messageCaptor.capture(), any(SendCallback.class));
        assertThat(messageCaptor.getValue().getPayload()).isEqualTo(msg);
    }

    @Test
    void sendRtpUpdateMessage_shouldNotPropagateExceptionWhenTemplateFails() {
        RtpUpdateMessage msg = new RtpUpdateMessage(
                new BigDecimal("10.00"), new BigDecimal("5.00"), "SLOT_01");

        doThrow(new RuntimeException("broker unavailable"))
                .when(gamesTemplate).asyncSend(any(), any(Message.class), any(SendCallback.class));

        assertThatNoException().isThrownBy(() -> producerService.sendRtpUpdateMessage(msg));
    }

    // ── sendSportTransactionMessage ─────────────────────────────────────────

    @Test
    void sendSportTransactionMessage_shouldSendToCorrectDestination() {
        SportTransactionMessage msg = new SportTransactionMessage(
                2L, 1L, SportTransactionType.SPORT_REGISTER,
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                new BigDecimal("1000.00"),
                "Initial sport balance", null);

        producerService.sendSportTransactionMessage(msg);

        String expected = RocketMQConstant.SPORT_TRANSACTION_TOPIC + ":" + RocketMQConstant.SPORT_TRANSACTION_TAG;
        verify(sportTemplate).asyncSend(eq(expected), messageCaptor.capture(), any(SendCallback.class));
        assertThat(messageCaptor.getValue().getPayload()).isEqualTo(msg);
    }

    @Test
    void sendSportTransactionMessage_shouldNotPropagateExceptionWhenTemplateFails() {
        SportTransactionMessage msg = new SportTransactionMessage(
                2L, 1L, SportTransactionType.SPORT_BET,
                new BigDecimal("50.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("950.00"),
                "Sport bet", null);

        doThrow(new RuntimeException("broker unavailable"))
                .when(sportTemplate).asyncSend(any(), any(Message.class), any(SendCallback.class));

        assertThatNoException().isThrownBy(() -> producerService.sendSportTransactionMessage(msg));
    }
}
