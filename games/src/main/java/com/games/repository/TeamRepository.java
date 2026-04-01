package com.games.repository;

import com.games.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * 根據外部隊伍ID查詢
     */
    Optional<Team> findByExternalTeamId(String externalTeamId);

    /**
     * 檢查外部隊伍ID是否存在
     */
    boolean existsByExternalTeamId(String externalTeamId);
}
