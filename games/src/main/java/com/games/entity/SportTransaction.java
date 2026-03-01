package com.games.entity;

import com.games.enums.SportTransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 體育投注交易流水表
 *
 * 記錄用戶在體育投注中的每筆交易：
 * - 投注扣款
 * - 派彩
 * - 退款
 * - 取消等
 */
@Entity
@Table(
        name = "sport_transactions",
        indexes = {
                @Index(name = "idx_sport_trans_user", columnList = "user_id"),
                @Index(name = "idx_sport_trans_bet", columnList = "sport_bet_id"),
                @Index(name = "idx_sport_trans_type", columnList = "type"),
                @Index(name = "idx_sport_trans_created", columnList = "created_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Comment("體育投注交易流水表")
public class SportTransaction {

    /** 交易ID，主鍵 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("交易ID，主鍵")
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

    /** 關聯體育投注ID FK → sport_bets */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_bet_id")
    @Comment("關聯體育投注ID")
    private SportBet sportBet;

    /**
     * 交易類型：
     * SPORT_BET-體育投注,
     * SPORT_WIN-體育派彩,
     * SPORT_REFUND-體育退款,
     * SPORT_CANCEL-體育取消
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    @Comment("交易類型：SPORT_BET-體育投注, SPORT_WIN-體育派彩, SPORT_REFUND-體育退款, SPORT_CANCEL-體育取消")
    private SportTransactionType type;

    /** 交易金額 */
    @Column(name = "amount", nullable = false, precision = 15, scale = 4)
    @Comment("交易金額")
    private BigDecimal amount;

    /** 交易前餘額 */
    @Column(name = "balance_before", precision = 15, scale = 4)
    @Comment("交易前餘額")
    private BigDecimal balanceBefore;

    /** 交易後餘額 */
    @Column(name = "balance_after", precision = 15, scale = 4)
    @Comment("交易後餘額")
    private BigDecimal balanceAfter;

    /** 交易說明 */
    @Column(name = "description", length = 255)
    @Comment("交易說明")
    private String description;

    /** 交易時間 */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("交易時間")
    private OffsetDateTime createdAt;

    /** 新增時自動填入交易時間 */
    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}