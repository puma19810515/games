package com.games.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLock {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   return redis.call('del', KEYS[1]) " +
                    "else " +
                    "   return 0 " +
                    "end";
    /**
     * 釋放分布式鎖
     */
    public void releaseLock(String key, String value) {
         redisTemplate.execute(
                new DefaultRedisScript<>(LUA_SCRIPT, Long.class),
                Collections.singletonList(key),
                value
        );
    }

    /**
     * 帶重試機制的分布式鎖
     * @param key
     * @param value
     * @param seconds
     * @param maxRetries
     * @return
     */
    public boolean tryLockWithRetry(String key, String value,
                                    long seconds, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            boolean locked = tryLock(key, value, seconds);
            if (locked) return true;

            try {
                // 指數退避: 100ms, 200ms, 400ms, 800ms
                long backoff = 100 * (1L << i);
                Thread.sleep(Math.min(backoff, 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * 嘗試取得分布式鎖
     */
    private boolean tryLock(String key, String value, long second) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, value, second, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

}
