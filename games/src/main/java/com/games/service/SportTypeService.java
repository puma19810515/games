package com.games.service;

import com.games.constant.RedisConstant;
import com.games.dto.OddsFormatResponse;
import com.games.dto.SportTypeResponse;
import com.games.entity.OddsFormat;
import com.games.entity.SportType;
import com.games.repository.SportTypeRepository;
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
public class SportTypeService {
    private final SportTypeRepository sportTypeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void refreshSportTypes() {
        log.info("Refreshing sport types...");
        Map<String, SportType> stringSportTypeMap = sportTypeRepository.findAll().stream()
                .collect(Collectors.toMap(SportType::getCode, st -> st));
        try {
            redisTemplate.opsForHash().putAll(RedisConstant.SPORT_TYPE_ALL, stringSportTypeMap);
        } catch (Exception e) {
            log.error("Failed to sport types settings to Redis", e);
        }
        log.info("Sport types refreshed successfully.");
    }

    public List<SportTypeResponse> findAll() {
        Map<Object, Object> sportTypeMap = redisTemplate.opsForHash()
                .entries(RedisConstant.SPORT_TYPE_ALL);
        return sportTypeMap.values().stream()
                .map(obj -> (SportType) obj)
                .map(st -> SportTypeResponse.builder()
                        .id(st.getId())
                        .code(st.getCode())
                        .name(st.getName())
                        .status(st.getStatus())
                        .displayOrder(st.getDisplayOrder())
                        .createdAt(st.getCreatedAt())
                        .updatedAt(st.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SportTypeResponse> updateStatus(String id, Integer status) {
        SportType sportType = sportTypeRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Sport type not found with id: " + id));
        sportType.setStatus(status);
        sportTypeRepository.save(sportType);
        sportTypeRepository.flush();
        refreshSportTypes();
        return findAll();
    }
}
