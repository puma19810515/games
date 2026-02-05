package com.games.repository;

import com.games.entity.Bet;
import com.games.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {

    @Query("select b from Bet b where b.user = :user and b.gameCode = :gameCode and b.createdAt >= :startTime and b.createdAt <= :endTime")
    Page<Bet> findByUserAndGameCode(@Param("user") User user,
                                    @Param("gameCode") String gameCode,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    Pageable pageable);

    @Query("select b from Bet b where b.user = :user and b.createdAt >= :startTime and b.createdAt <= :endTime")
    Page<Bet> findByUser(@Param("user") User user, @Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime, Pageable pageable);
}
