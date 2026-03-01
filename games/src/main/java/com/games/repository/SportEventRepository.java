package com.games.repository;

import com.games.entity.SportEvent;
import com.games.enums.SportEventBettingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SportEventRepository extends JpaRepository<SportEvent, Long> {

    /**
     * 根據ID查詢賽事（包含關聯資料）
     */
    @Query("SELECT e FROM SportEvent e " +
           "LEFT JOIN FETCH e.sportType " +
           "LEFT JOIN FETCH e.league " +
           "LEFT JOIN FETCH e.homeTeam " +
           "LEFT JOIN FETCH e.awayTeam " +
           "WHERE e.id = :id")
    Optional<SportEvent> findByIdWithDetails(@Param("id") Long id);

    /**
     * 查詢可投注的賽事
     */
    @Query("SELECT e FROM SportEvent e " +
           "LEFT JOIN FETCH e.sportType st " +
           "LEFT JOIN FETCH e.league " +
           "WHERE e.sportEventBettingStatus = 'OPEN' " +
           "AND (:sportTypeCode IS NULL OR st.code = :sportTypeCode) " +
           "AND e.startTime > :now " +
           "ORDER BY e.startTime ASC")
    List<SportEvent> findOpenEvents(
            @Param("sportTypeCode") String sportTypeCode,
            @Param("now") LocalDateTime now
    );

    /**
     * 分頁查詢賽事
     */
    @Query("SELECT e FROM SportEvent e " +
           "LEFT JOIN FETCH e.sportType st " +
           "LEFT JOIN FETCH e.league l " +
           "WHERE e.sportEventBettingStatus = :bettingStatus " +
           "AND (:sportTypeId IS NULL OR st.id = :sportTypeId) " +
           "AND (:leagueId IS NULL OR l.id = :leagueId) " +
           "ORDER BY e.startTime ASC")
    Page<SportEvent> findByFilters(
            @Param("bettingStatus") SportEventBettingStatus bettingStatus,
            @Param("sportTypeId") Long sportTypeId,
            @Param("leagueId") Long leagueId,
            Pageable pageable
    );

    /**
     * 查詢已結束但未結算的賽事
     */
    @Query("SELECT e FROM SportEvent e " +
           "WHERE e.sportEventStatus = 'FINISHED' AND e.sportEventSettleStatus = 'UNSETTLED'")
    List<SportEvent> findFinishedUnsettled();
}
