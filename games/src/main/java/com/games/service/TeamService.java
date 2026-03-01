package com.games.service;


import com.games.constant.RedisConstant;
import com.games.entity.Team;
import com.games.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public void refreshTeams() {
        log.info("Refreshing sport types...");
        Map<String, String> teamMap = new HashMap<>();
        List<Team> teamLt = teamRepository.findAll();
        for (Team team : teamLt) {
            teamMap.put(team.getSportType().getCode() + "_" + team.getName(), team.getName());
        }
        try {
            redisTemplate.opsForHash().putAll(RedisConstant.TEAM_ALL, teamMap);
        } catch (Exception e) {
            log.error("Failed to sport types settings to Redis", e);
        }
        log.info("Sport types refreshed successfully.");
    }
}
