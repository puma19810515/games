package com.games.repository;

import com.games.annotation.ReadOnly;
import com.games.dto.MerchantProfitDto;
import com.games.entity.Bet;
import com.games.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {

    @ReadOnly
    @Query("SELECT b FROM Bet b WHERE b.user = :user AND b.gameCode = :gameCode AND b.createdAt >= :startTime AND b.createdAt <= :endTime")
    Page<Bet> findByUserAndGameCode(@Param("user") User user,
                                    @Param("gameCode") String gameCode,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    Pageable pageable);

    @ReadOnly
    @Query("SELECT b FROM Bet b WHERE b.user = :user AND b.createdAt >= :startTime AND b.createdAt <= :endTime")
    Page<Bet> findByUser(@Param("user") User user, @Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT new com.games.dto.MerchantProfitDto(COALESCE(SUM(b.betAmount), 0), COALESCE(SUM(b.winAmount), 0)) FROM Bet b WHERE b.merchant.id = :merId AND b.createdAt >= :startOfDay AND b.createdAt < :endOfDay ")
    MerchantProfitDto findMerchantProfit(@Param("merId") Long merchantId,
                                              @Param("startOfDay") LocalDateTime startOfDay,
                                              @Param("endOfDay") LocalDateTime endOfDay);
}
