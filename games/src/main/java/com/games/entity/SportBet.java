package com.games.entity;

import com.games.enums.SportBetStatus;
import com.games.enums.SportBetType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 體育投注主表
 *
 * 記錄單注(SINGLE)或串關(PARLAY)投注資訊
 * 包含投注金額、總賠率、預計贏取金額、實際贏取金額、狀態等
 */
@Entity
@Table(
        name = "sport_bets",
        indexes = {
                @Index(name = "idx_sport_bets_merchant", columnList = "merchant_id"),
                @Index(name = "idx_sport_bets_user", columnList = "user_id"),
                @Index(name = "idx_sport_bets_status", columnList = "status"),
                @Index(name = "idx_sport_bets_placed_at", columnList = "placed_at"),
                @Index(name = "idx_sport_bets_user_placed", columnList = "user_id, placed_at")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Comment("體育投注主表")
public class SportBet {

    /** 投注ID，主鍵 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("投注ID，主鍵")
    private Long id;

    /** 商戶ID FK → merchants */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    @Comment("商戶ID")
    private Merchant merchant;

    /** 會員ID FK → users */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("會員ID")
    private User user;

    /** 投注類型：SINGLE-單注, PARLAY-串關 */
    @Enumerated(EnumType.STRING)
    @Column(name = "bet_type", nullable = false, length = 20)
    @Comment("投注類型：SINGLE-單注, PARLAY-串關")
    private SportBetType betType;

    /** 投注金額 */
    @Column(name = "stake", nullable = false, precision = 15, scale = 4)
    @Comment("投注金額")
    private BigDecimal stake;

    /** 總賠率（串關為各腿相乘） */
    @Column(name = "total_odds", precision = 15, scale = 4)
    @Comment("總賠率（串關為各腿相乘）")
    private BigDecimal totalOdds;

    /** 預計最大贏取金額 */
    @Column(name = "potential_win", precision = 15, scale = 4)
    @Comment("預計最大贏取金額")
    private BigDecimal potentialWin;

    /** 實際贏取金額，預設 0 */
    @Column(name = "win_amount", precision = 15, scale = 4)
    @Comment("實際贏取金額")
    private BigDecimal winAmount = BigDecimal.ZERO;

    /** 有效投注額 */
    @Column(name = "valid_bet", precision = 15, scale = 4)
    @Comment("有效投注額")
    private BigDecimal validBet;

    /**
     * 投注狀態
     * - PENDING：待結算
     * - SETTLED：已結算
     * - CANCELLED：已取消
     * - CASHED_OUT：提前結算
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Comment("投注狀態：PENDING-待結算, SETTLED-已結算, CANCELLED-已取消, CASHED_OUT-提前結算")
    private SportBetStatus status = SportBetStatus.PENDING;

    /** 提前兌現金額 */
    @Column(name = "cashout_amount", precision = 15, scale = 4)
    @Comment("提前兌現金額")
    private BigDecimal cashoutAmount;

    /** 取消原因 */
    @Column(name = "cancel_reason", length = 255)
    @Comment("取消原因")
    private String cancelReason;

    /** 下注時間 */
    @Column(name = "placed_at", nullable = false)
    @Comment("下注時間")
    private LocalDateTime placedAt;

    /** 結算時間 */
    @Column(name = "settled_at")
    @Comment("結算時間")
    private LocalDateTime settledAt;

    /** 取消時間 */
    @Column(name = "cancelled_at")
    @Comment("取消時間")
    private LocalDateTime cancelledAt;

    /** 建立時間 */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("建立時間")
    private LocalDateTime createdAt;

    /** 更新時間 */
    @Column(name = "updated_at", nullable = false)
    @Comment("更新時間")
    private LocalDateTime updatedAt;

    /** 自動填入建立時間和更新時間 */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.placedAt == null) {
            this.placedAt = now;
        }
    }

    /** 自動更新 updated_at */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}