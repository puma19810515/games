package com.games.repository;

import com.games.entity.SportBet;
import com.games.enums.SportBetStatus;
import com.games.enums.SportBetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SportBetRepository extends JpaRepository<SportBet, Long>, JpaSpecificationExecutor<SportBet> {

    /**
     * 根據ID和用戶ID查詢投注（確保用戶只能查看自己的投注）
     */
    @Query("SELECT sb FROM SportBet sb WHERE sb.id = :betId AND sb.user.id = :userId")
    Optional<SportBet> findByIdAndUserId(@Param("betId") Long betId, @Param("userId") Long userId);

    /**
     * 分頁查詢用戶投注記錄
     */
    @Query("SELECT sb FROM SportBet sb WHERE sb.user.id = :userId ORDER BY sb.placedAt DESC")
    Page<SportBet> findByUserIdOrderByPlacedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * 查詢待結算的投注（用於結算排程）
     */
    @Query("SELECT sb FROM SportBet sb WHERE sb.status = 'PENDING'")
    List<SportBet> findAllPending();

    /**
     * 根據賽事ID查詢待結算投注
     */
    @Query("SELECT DISTINCT sb FROM SportBet sb " +
           "JOIN sb.user u " +
           "JOIN BetLeg bl ON bl.bet = sb " +
           "WHERE bl.event.id = :eventId AND sb.status = 'PENDING'")
    List<SportBet> findPendingByEventId(@Param("eventId") Long eventId);
}