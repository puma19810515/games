package com.games.repository;

import com.games.entity.BetLeg;
import com.games.enums.SettlementResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetLegRepository extends JpaRepository<BetLeg, Long> {

    /**
     * 根據投注ID查詢所有投注腿
     */
    @Query("SELECT bl FROM BetLeg bl " +
           "LEFT JOIN FETCH bl.event e " +
           "LEFT JOIN FETCH e.sportType " +
           "LEFT JOIN FETCH e.league " +
           "LEFT JOIN FETCH bl.marketLine ml " +
           "LEFT JOIN FETCH ml.betType " +
           "LEFT JOIN FETCH ml.oddsFormat " +
           "WHERE bl.bet.id = :betId")
    List<BetLeg> findByBetIdWithDetails(@Param("betId") Long betId);

    /**
     * 根據投注ID列表查詢所有投注腿
     */
    @Query("SELECT bl FROM BetLeg bl " +
           "LEFT JOIN FETCH bl.event e " +
           "LEFT JOIN FETCH e.sportType " +
           "LEFT JOIN FETCH e.league " +
           "LEFT JOIN FETCH bl.marketLine ml " +
           "LEFT JOIN FETCH ml.betType " +
           "WHERE bl.bet.id IN :betIds")
    List<BetLeg> findByBetIdsWithDetails(@Param("betIds") List<Long> betIds);

    /**
     * 根據賽事ID查詢待結算的投注腿
     */
    @Query("SELECT bl FROM BetLeg bl " +
           "WHERE bl.event.id = :eventId AND bl.result = 'PENDING'")
    List<BetLeg> findPendingByEventId(@Param("eventId") Long eventId);

}
