package com.games.rocketmq.consumer;

import com.games.dto.RtpUpdateMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RtpUpdateMessageConsumerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RtpUpdateMessageConsumer consumer;

    private static final String RTP_TOTAL_BET_KEY = "rtp:total:bet";
    private static final String RTP_TOTAL_WIN_KEY = "rtp:total:win";
    private static final String RTP_BET_COUNT_KEY = "rtp:total:count";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        consumer = new RtpUpdateMessageConsumer(redisTemplate);
    }

    // ── happy path ──────────────────────────────────────────────────────────

    @Test
    void onMessage_shouldIncrementAllThreeRedisKeys() {
        RtpUpdateMessage msg = new RtpUpdateMessage(
                new BigDecimal("200.00"),
                new BigDecimal("150.00"),
                "SLOT_01");

        consumer.onMessage(msg);

        verify(valueOperations).increment(RTP_TOTAL_BET_KEY, 200.0);
        verify(valueOperations).increment(RTP_TOTAL_WIN_KEY, 150.0);
        verify(valueOperations).increment(eq(RTP_BET_COUNT_KEY), eq(1L));
    }

    @Test
    void onMessage_shouldSetTtlOnAllThreeKeys() {
        RtpUpdateMessage msg = new RtpUpdateMessage(
                new BigDecimal("100.00"),
                new BigDecimal("80.00"),
                "SLOT_02");

        consumer.onMessage(msg);

        verify(redisTemplate).expire(RTP_TOTAL_BET_KEY, 30L, TimeUnit.DAYS);
        verify(redisTemplate).expire(RTP_TOTAL_WIN_KEY, 30L, TimeUnit.DAYS);
        verify(redisTemplate).expire(RTP_BET_COUNT_KEY, 30L, TimeUnit.DAYS);
    }

    @Test
    void onMessage_whenBetAmountIsNull_shouldIncrementByZero() {
        RtpUpdateMessage msg = new RtpUpdateMessage(null, new BigDecimal("10.00"), "SLOT_03");

        consumer.onMessage(msg);

        verify(valueOperations).increment(RTP_TOTAL_BET_KEY, 0.0);
        verify(valueOperations).increment(RTP_TOTAL_WIN_KEY, 10.0);
    }

    @Test
    void onMessage_whenWinAmountIsNull_shouldIncrementByZero() {
        RtpUpdateMessage msg = new RtpUpdateMessage(new BigDecimal("50.00"), null, "SLOT_04");

        consumer.onMessage(msg);

        verify(valueOperations).increment(RTP_TOTAL_BET_KEY, 50.0);
        verify(valueOperations).increment(RTP_TOTAL_WIN_KEY, 0.0);
    }

    // ── error path ──────────────────────────────────────────────────────────

    @Test
    void onMessage_whenRedisFails_shouldThrowForRetry() {
        RtpUpdateMessage msg = new RtpUpdateMessage(
                new BigDecimal("50.00"), new BigDecimal("30.00"), "SLOT_05");

        doThrow(new RuntimeException("Redis connection refused"))
                .when(valueOperations).increment(anyString(), anyDouble());

        assertThatThrownBy(() -> consumer.onMessage(msg))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process RTP update message");
    }
}
