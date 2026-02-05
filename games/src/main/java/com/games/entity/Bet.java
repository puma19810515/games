package com.games.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bet {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal betAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal winAmount;

    @Column(nullable = false, length = 500)
    private String result;

    @Column(nullable = false)
    private Boolean isWin;

    @Column(name = "game_code", nullable = false)
    private String gameCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
