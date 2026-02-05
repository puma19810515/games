package com.games.rocketmq.consumer;

import com.games.constant.RocketMQConstant;
import com.games.dto.RtpUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = RocketMQConstant.RTP_UPDATE_TOPIC,
        consumerGroup = "${rocketmq.consumer.group}",
        selectorExpression = RocketMQConstant.RTP_TAG
)
public class
RtpUpdateMessageConsumer implements RocketMQListener<RtpUpdateMessage> {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RTP_TOTAL_BET_KEY = "rtp:total:bet";
    private static final String RTP_TOTAL_WIN_KEY = "rtp:total:win";
    private static final String RTP_BET_COUNT_KEY = "rtp:total:count";
    private static final long RTP_TTL_DAYS = 30;

    @Override
    public void onMessage(RtpUpdateMessage message) {
        try {
            log.debug("Processing RTP update message: betAmount={}, winAmount={}",
                    message.getBetAmount(), message.getWinAmount());

            // 更新 Redis 统计数据（原子操作）
            redisTemplate.opsForValue().increment(RTP_TOTAL_BET_KEY, message.getBetAmount().doubleValue());
            redisTemplate.opsForValue().increment(RTP_TOTAL_WIN_KEY, message.getWinAmount().doubleValue());
            redisTemplate.opsForValue().increment(RTP_BET_COUNT_KEY, 1);

            // 设置过期时间（30天）
            redisTemplate.expire(RTP_TOTAL_BET_KEY, RTP_TTL_DAYS, TimeUnit.DAYS);
            redisTemplate.expire(RTP_TOTAL_WIN_KEY, RTP_TTL_DAYS, TimeUnit.DAYS);
            redisTemplate.expire(RTP_BET_COUNT_KEY, RTP_TTL_DAYS, TimeUnit.DAYS);

            log.debug("RTP update message processed successfully");
        } catch (Exception e) {
            log.error("Failed to process RTP update message: {}", e.getMessage(), e);
            // 抛出异常会触发 RocketMQ 重试机制
            throw new RuntimeException("Failed to process RTP update message", e);
        }
    }
}


