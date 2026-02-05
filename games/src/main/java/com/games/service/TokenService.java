package com.games.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务
 * 使用 Redis 存储和验证 JWT Token
 *
 * 注意：此服务已从 DataSourceAspect 的读写分离机制中排除，
 * 因为它只使用 Redis，不涉及 MySQL 主从数据库操作。
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.expiration}")
    private Long expiration;

    private static final String TOKEN_PREFIX = "token:";

    public void storeToken(String username, String token) {
        String key = TOKEN_PREFIX + username;
        redisTemplate.opsForValue().set(key, token, expiration, TimeUnit.MILLISECONDS);
    }

    public boolean validateToken(String username, String token) {
        String key = TOKEN_PREFIX + username;
        Object storedToken = redisTemplate.opsForValue().get(key);
        return token.equals(storedToken);
    }

    public void removeToken(String username) {
        String key = TOKEN_PREFIX + username;
        redisTemplate.delete(key);
    }

    public String getToken(String username) {
        String key = TOKEN_PREFIX + username;
        Object token = redisTemplate.opsForValue().get(key);
        return token != null ? token.toString() : null;
    }
}
