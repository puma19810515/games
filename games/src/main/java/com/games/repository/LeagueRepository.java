package com.games.repository;

import com.games.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {

    /**
     * 根據外部聯賽ID查詢
     */
    Optional<League> findByExternalLeagueId(String externalLeagueId);

    /**
     * 檢查外部聯賽ID是否存在
     */
    boolean existsByExternalLeagueId(String externalLeagueId);
}
