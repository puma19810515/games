package com.games.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_setting",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_game_code", columnNames = "game_code")
        },
        indexes = {
                @Index(name = "idx_game_code", columnList = "game_code"),
                @Index(name = "idx_created_at", columnList = "created_at")
        })
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GameSetting {

    @Id
    private Long id;

    @Column(name = "game_code", nullable = false)
    private String gameCode;

    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Minimum bet amount
     */
    @Column(name = "min_bet", nullable = false, precision = 15, scale = 2)
    private BigDecimal minBet;

    /**
     * Maximum bet amount
     */
    @Column(name = "max_bet", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxBet;

    /**
     * Return to Player percentage
     */
    @Column(name = "rtp_set", nullable = false, scale = 2)
    private Double rtpSet;

    @Lob
    @Column(name = "game_settings", nullable = false, columnDefinition = "TEXT")
    private String gameSettings;

    @Column(name = "two_match_multiplier", nullable = false, precision = 15, scale = 2)
    private BigDecimal twoMatchMultiplier;

    /**
     * 1. Slot machines
     * 2. Fruit platter
     */
    @Column(name = "category", nullable = false)
    private Integer category;

    /**
     * 0 = Disabled
     * 1 = Enabled
     * 2 = Maintenance
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
