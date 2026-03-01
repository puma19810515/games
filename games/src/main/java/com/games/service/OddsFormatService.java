package com.games.service;

import com.games.constant.RedisConstant;
import com.games.dto.OddsFormatResponse;
import com.games.entity.OddsFormat;
import com.games.repository.OddsFormatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OddsFormatService {
    private final OddsFormatRepository oddsFormatRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void refreshOddsFormats() {
        log.info("Refreshing odds formats...");
        Map<String, OddsFormat> oddsFormatMap = oddsFormatRepository.findAll().stream()
                .collect(Collectors.toMap(OddsFormat::getCode, of -> of));
        try {
            redisTemplate.opsForHash().putAll(RedisConstant.SPORT_ODDS_FORMAT_ALL, oddsFormatMap);
        } catch (Exception e) {
            log.error("Failed to refresh odds formats settings to Redis", e);
        }
        log.info("Odds formats refreshed successfully.");
    }

    public List<OddsFormatResponse> findAll() {
        Map<Object, Object> oddsFormatMap = redisTemplate.opsForHash()
                .entries(RedisConstant.SPORT_ODDS_FORMAT_ALL);
        return oddsFormatMap.values().stream()
                .map(obj -> (OddsFormat) obj)
                .map(of -> OddsFormatResponse.builder()
                        .id(of.getId())
                        .code(of.getCode())
                        .name(of.getName())
                        .nameEn(of.getNameEn())
                        .description(of.getDescription())
                        .status(of.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    public List<OddsFormatResponse> updateStatus(String id, Integer status) {
        OddsFormat oddsFormat = oddsFormatRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Odds format not found with id: " + id));
        oddsFormat.setStatus(status);
        oddsFormatRepository.save(oddsFormat);
        oddsFormatRepository.flush();
        refreshOddsFormats();
        return findAll();
    }
}
