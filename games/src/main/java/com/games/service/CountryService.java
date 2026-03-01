package com.games.service;

import com.games.constant.RedisConstant;
import com.games.entity.Country;
import com.games.entity.SportType;
import com.games.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryService {

    private final CountryRepository countryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void refreshCountries() {
        log.info("Refreshing countries...");
        // 这里可以添加从数据库获取国家数据并刷新到 Redis 的逻辑
        Map<String, Country> countryMap = countryRepository.findAll().stream()
                .collect(Collectors.toMap(Country::getCode, st -> st));
        try {
            redisTemplate.opsForHash().putAll(RedisConstant.COUNTRY_ALL, countryMap);
        } catch (Exception e) {
            log.error("Failed to countries types settings to Redis", e);
        }
        log.info("Countries refreshed successfully.");
    }
}
