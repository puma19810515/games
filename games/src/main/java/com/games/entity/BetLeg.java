package com.games.entity;

import com.games.enums.BetLegResult;
import com.games.enums.BetLegSelection;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投注明細表（每一腿）
 *
 * 用途：
 * 1. 拆分單注或串關投注
 * 2. 結算每一腿的結果
 * 3. 支援計算串關總賠率和最終贏額
 */
@Entity
@Table(
        name = "bet_legs",
        indexes = {
                @Index(name = "idx_bet_legs_bet", columnList = "bet_id"),
                @Index(name = "idx_bet_legs_event", columnList = "event_id"),
                @Index(name = "idx_bet_legs_result", columnList = "result")
        }
)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Comment("投注明細表（每一腿）")
public class BetLeg {

    /** 明細ID，主鍵 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("明細ID，主鍵")
    private Long id;

    /** 所屬投注ID */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bet_id", nullable = false)
    @Comment("所屬投注ID")
    private SportBet bet;

    /** 賽事ID */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    @Comment("賽事ID")
    private SportEvent event;

    /** 盤口ID */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_line_id", nullable = false)
    @Comment("盤口ID")
    private MarketLine marketLine;

    /** 玩法代碼 */
    @Column(name = "bet_type_code", nullable = false, length = 30)
    @Comment("玩法代碼")
    private String betTypeCode;

    /** 賠率格式代碼 */
    @Column(name = "odds_format_code", nullable = false, length = 20)
    @Comment("賠率格式代碼")
    private String oddsFormatCode;

    /** 選擇項：HOME-主隊, AWAY-客隊, OVER-大, UNDER-小, DRAW-平, YES-是, NO-否, ODD-單, EVEN-雙 */
    @Enumerated(EnumType.STRING)
    @Column(name = "selection", nullable = false, length = 20)
    @Comment("選擇項：HOME-主隊, AWAY-客隊, OVER-大, UNDER-小, DRAW-平, YES-是, NO-否, ODD-單, EVEN-雙")
    private BetLegSelection selection;

    /** 讓球/大小值 */
    @Column(name = "handicap", precision = 5, scale = 2)
    @Comment("讓球/大小值")
    private BigDecimal handicap;

    /** 原始賠率（依賠率格式） */
    @Column(name = "odds", nullable = false, precision = 10, scale = 4)
    @Comment("原始賠率（依賠率格式）")
    private BigDecimal odds;

    /** 轉換後的歐洲盤賠率（用於統一計算） */
    @Column(name = "odds_decimal", nullable = false, precision = 10, scale = 4)
    @Comment("轉換後的歐洲盤賠率（用於統一計算）")
    private BigDecimal oddsDecimal;

    /**
     * 結果：
     * WIN-贏, LOSE-輸, PUSH-和, HALF_WIN-半贏, HALF_LOSE-半輸, VOID-作廢, PENDING-待定
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 20)
    @Comment("結果：WIN-贏, LOSE-輸, PUSH-和, HALF_WIN-半贏, HALF_LOSE-半輸, VOID-作廢, PENDING-待定")
    private BetLegResult result = BetLegResult.PENDING;

    /**
     * 結算係數：
     * WIN=1.0, HALF_WIN=0.5, PUSH=0(退款), HALF_LOSE=-0.5, LOSE=-1.0, VOID=0(退款)
     */
    @Column(name = "result_factor", precision = 5, scale = 2)
    @Comment("結算係數：WIN=1.0, HALF_WIN=0.5, PUSH=0(退款), HALF_LOSE=-0.5, LOSE=-1.0, VOID=0(退款)")
    private BigDecimal resultFactor = BigDecimal.ONE;

    /** 建立時間 */
    @Column(name = "created_at", nullable = false)
    @Comment("建立時間")
    private LocalDateTime createdAt;

    /** 更新時間 */
    @Column(name = "updated_at", nullable = false)
    @Comment("更新時間")
    private LocalDateTime updatedAt;

    /** 新增時自動填入建立時間和更新時間 */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** 更新時自動刷新 updated_at */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}