package com.games.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.config.GameProperties;
import com.games.constant.RedisConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RtpStatisticsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public Map<String, Object> getStatistics(String gameCode) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 從 Redis 讀取遊戲配置
            String json = (String) redisTemplate.opsForHash().get(RedisConstant.GAME_SETTING_ALL, gameCode);
            if (json == null) {
                log.error("Game configuration not found for gameCode: {}", gameCode);
                stats.put("error", "Game configuration not found");
                return stats;
            }
            GameProperties gameProperties = objectMapper.readValue(json, GameProperties.class);

            Object totalBetObj = redisTemplate.opsForValue().get(RedisConstant.RTP_TOTAL_BET_KEY);
            Object totalWinObj = redisTemplate.opsForValue().get(RedisConstant.RTP_TOTAL_WIN_KEY);
            Object betCountObj = redisTemplate.opsForValue().get(RedisConstant.RTP_BET_COUNT_KEY);

            double totalBet = totalBetObj != null ? Double.parseDouble(totalBetObj.toString()) : 0.0;
            double totalWin = totalWinObj != null ? Double.parseDouble(totalWinObj.toString()) : 0.0;
            long betCount = betCountObj != null ? Long.parseLong(betCountObj.toString()) : 0;

            double actualRtp = totalBet > 0 ? (totalWin / totalBet) * 100 : 0.0;

            stats.put("targetRtp", gameProperties.getTargetRtp());
            stats.put("actualRtp", BigDecimal.valueOf(actualRtp).setScale(2, RoundingMode.HALF_UP).doubleValue());
            stats.put("totalBetAmount", BigDecimal.valueOf(totalBet).setScale(2, RoundingMode.HALF_UP));
            stats.put("totalWinAmount", BigDecimal.valueOf(totalWin).setScale(2, RoundingMode.HALF_UP));
            stats.put("totalBetCount", betCount);
            stats.put("averageBet", betCount > 0 ?
                    BigDecimal.valueOf(totalBet / betCount).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            stats.put("averageWin", betCount > 0 ?
                    BigDecimal.valueOf(totalWin / betCount).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

            double rtpDifference = actualRtp - gameProperties.getTargetRtp();
            stats.put("rtpDifference", BigDecimal.valueOf(rtpDifference).setScale(2, RoundingMode.HALF_UP).doubleValue());
            stats.put("rtpStatus", getRtpStatus(rtpDifference));

            log.debug("RTP Statistics: {}", stats);
        } catch (Exception e) {
            log.error("Failed to get RTP statistics", e);
            stats.put("error", "Failed to retrieve statistics");
        }

        return stats;
    }

    private String getRtpStatus(double difference) {
        if (Math.abs(difference) <= 2.0) {
            return "OPTIMAL";
        } else if (difference > 2.0) {
            return "HIGH";
        } else {
            return "LOW";
        }
    }

    public void resetStatistics(String gameCode) {
        try {
            redisTemplate.delete(RedisConstant.RTP_TOTAL_BET_KEY);
            redisTemplate.delete(RedisConstant.RTP_TOTAL_WIN_KEY);
            redisTemplate.delete(RedisConstant.RTP_BET_COUNT_KEY);
            log.info("RTP statistics reset successfully");
        } catch (Exception e) {
            log.error("Failed to reset RTP statistics", e);
        }
    }
}
