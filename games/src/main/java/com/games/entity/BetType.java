package com.games.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bet_types",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_bet_types_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_bet_types_code", columnList = "code"),
                @Index(name = "idx_bet_types_status", columnList = "status"),
                @Index(name = "idx_bet_types_odds_format", columnList = "odds_format_id")
        }
)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BetType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 唯一代碼
     * 例如:
     * MATCH_WINNER
     * HANDICAP
     * OVER_UNDER
     */
    @Column(nullable = false, length = 30)
    private String code;

    /**
     * 顯示名稱
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * FK → odds_formats
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "odds_format_id")
    private OddsFormat oddsFormat;

    /**
     * 描述
     */
    @Column(length = 255)
    private String description;

    /**
     * 狀態
     * 0 = 停用
     * 1 = 啟用
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 建立時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
    }

}