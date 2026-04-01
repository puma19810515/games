package com.games.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 盤口賠率線表
 * 由爬蟲服務抓取外部盤口後寫入
 *
 * 支援玩法：
 * - 讓球盤
 * - 大小盤
 * - 歐洲盤
 * - 單雙
 * - 是/否
 * - 波膽(JSON)
 */
@Entity
@Table(
        name = "market_lines",
        indexes = {
                @Index(name = "idx_market_event", columnList = "event_id"),
                @Index(name = "idx_market_active", columnList = "is_active"),
                @Index(name = "idx_market_event_bettype", columnList = "event_id, bet_type_id")
        }
)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Comment("盤口賠率線表（由爬蟲服務填入）")
public class MarketLine {

    /** 盤口ID，主鍵 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("盤口ID，主鍵")
    private Long id;

    /** 賽事ID */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    @Comment("賽事ID")
    private SportEvent event;

    /** 玩法類型ID */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bet_type_id", nullable = false)
    @Comment("玩法類型ID")
    private BetType betType;

    /** 賠率格式ID（歐盤 / 香港盤 / 馬來盤） */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "odds_format_id", nullable = false)
    @Comment("賠率格式ID")
    private OddsFormat oddsFormat;

    /** 外部盤口ID（爬蟲來源） */
    @Column(name = "external_market_id", length = 50)
    @Comment("外部盤口ID（爬蟲來源）")
    private String externalMarketId;

    /** 讓球/大小值，如 -0.5, 0.25, 2.5, 215.5 */
    @Column(precision = 5, scale = 2)
    @Comment("讓球/大小值")
    private BigDecimal handicap;

    /** 主隊賠率 */
    @Column(name = "home_odds", precision = 10, scale = 4)
    @Comment("主隊賠率")
    private BigDecimal homeOdds;

    /** 客隊賠率 */
    @Column(name = "away_odds", precision = 10, scale = 4)
    @Comment("客隊賠率")
    private BigDecimal awayOdds;

    /** 和局賠率（歐洲盤） */
    @Column(name = "draw_odds", precision = 10, scale = 4)
    @Comment("和局賠率")
    private BigDecimal drawOdds;

    /** 大盤賠率 */
    @Column(name = "over_odds", precision = 10, scale = 4)
    @Comment("大盤賠率")
    private BigDecimal overOdds;

    /** 小盤賠率 */
    @Column(name = "under_odds", precision = 10, scale = 4)
    @Comment("小盤賠率")
    private BigDecimal underOdds;

    /** 是賠率（兩隊進球等） */
    @Column(name = "yes_odds", precision = 10, scale = 4)
    @Comment("是賠率")
    private BigDecimal yesOdds;

    /** 否賠率 */
    @Column(name = "no_odds", precision = 10, scale = 4)
    @Comment("否賠率")
    private BigDecimal noOdds;

    /** 單數賠率 */
    @Column(name = "odd_odds", precision = 10, scale = 4)
    @Comment("單數賠率")
    private BigDecimal oddOdds;

    /** 雙數賠率 */
    @Column(name = "even_odds", precision = 10, scale = 4)
    @Comment("雙數賠率")
    private BigDecimal evenOdds;

    /**
     * 波膽賠率
     * JSON格式，例如：
     * {
     *   "1-0": 7.50,
     *   "2-1": 9.50
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_odds", columnDefinition = "jsonb")
    @Comment("波膽賠率(JSON格式)")
    private Map<String, Object> scoreOdds;

    /** 是否有效 */
    @Column(name = "is_active", nullable = false)
    @Comment("是否有效：TRUE-有效, FALSE-停用")
    private Boolean isActive = true;

    /** 建立時間 */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("建立時間")
    private LocalDateTime createdAt;

    /** 更新時間 */
    @Column(name = "updated_at", nullable = false)
    @Comment("更新時間")
    private LocalDateTime updatedAt;

    /** 建立時自動填入時間 */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** 更新時自動刷新時間 */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}