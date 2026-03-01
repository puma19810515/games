package com.games.repository;

import com.games.entity.MarketLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketLineRepository extends JpaRepository<MarketLine, Long> {

    /**
     * 根據ID查詢盤口（包含關聯資料）
     */
    @Query("SELECT ml FROM MarketLine ml " +
           "LEFT JOIN FETCH ml.event e " +
           "LEFT JOIN FETCH e.sportType " +
           "LEFT JOIN FETCH e.league " +
           "LEFT JOIN FETCH ml.betType bt " +
           "LEFT JOIN FETCH ml.oddsFormat " +
           "WHERE ml.id = :id AND ml.isActive = true")
    Optional<MarketLine> findByIdWithDetails(@Param("id") Long id);

    /**
     * 查詢賽事的所有有效盤口
     */
    @Query("SELECT ml FROM MarketLine ml " +
           "LEFT JOIN FETCH ml.betType bt " +
           "LEFT JOIN FETCH ml.oddsFormat " +
           "WHERE ml.event.id = :eventId AND ml.isActive = true " +
           "ORDER BY bt.code, ml.handicap")
    List<MarketLine> findActiveByEventId(@Param("eventId") Long eventId);

    /**
     * 查詢賽事特定玩法的盤口
     */
    @Query("SELECT ml FROM MarketLine ml " +
           "LEFT JOIN FETCH ml.betType bt " +
           "LEFT JOIN FETCH ml.oddsFormat " +
           "WHERE ml.event.id = :eventId AND bt.code = :betTypeCode AND ml.isActive = true " +
           "ORDER BY ml.handicap")
    List<MarketLine> findByEventIdAndBetTypeCode(
            @Param("eventId") Long eventId,
            @Param("betTypeCode") String betTypeCode
    );
}