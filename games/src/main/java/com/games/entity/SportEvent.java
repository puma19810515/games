package com.games.entity;

import com.games.enums.SportEventBettingStatus;
import com.games.enums.SportEventSettleStatus;
import com.games.enums.SportEventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 賽事表（SportEvent）
 *
 * 主要存放比賽資訊、隊伍、比分、狀態及結算資訊
 * 由爬蟲或系統管理員填入
 */
@Entity
@Table(
        name = "sport_events",
        indexes = {
                @Index(name = "idx_sport_events_sport_type", columnList = "sport_type_id"),
                @Index(name = "idx_sport_events_league", columnList = "league_id"),
                @Index(name = "idx_sport_events_start_time", columnList = "start_time"),
                @Index(name = "idx_sport_events_status", columnList = "status"),
                @Index(name = "idx_sport_events_settle_status", columnList = "settle_status"),
                @Index(name = "idx_sport_events_betting_status", columnList = "betting_status"),
                @Index(name = "idx_sport_events_external_id", columnList = "external_event_id")
        }
)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SportEvent {

    /** 賽事ID，主鍵 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → sport_types，賽事類型ID */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_type_id", nullable = false)
    private SportType sportType;

    /** FK → leagues，聯賽ID，可為 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;

    /** 外部比賽ID（爬蟲來源），唯一 */
    @Column(name = "external_event_id", length = 50, unique = true)
    private String externalEventId;

    /** 主隊 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    /** 客隊 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    /** 冗餘欄位：主隊名稱（提升查詢效能） */
    @Column(name = "home_team_name", nullable = false, length = 100)
    private String homeTeamName;

    /** 冗餘欄位：客隊名稱（提升查詢效能） */
    @Column(name = "away_team_name", nullable = false, length = 100)
    private String awayTeamName;

    /** 開賽時間 */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /** 全場比分：主隊得分 */
    @Column(name = "home_score")
    private Integer homeScore;

    /** 全場比分：客隊得分 */
    @Column(name = "away_score")
    private Integer awayScore;

    /** 半場比分：主隊得分 */
    @Column(name = "home_score_half")
    private Integer homeScoreHalf;

    /** 半場比分：客隊得分 */
    @Column(name = "away_score_half")
    private Integer awayScoreHalf;

    /**
     * 比賽狀態
     * - UPCOMING：未開賽
     * - LIVE：進行中
     * - FINISHED：已結束
     * - CANCELLED：取消
     * - POSTPONED：延期
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SportEventStatus sportEventStatus;

    /**
     * 投注狀態
     * OPEN : 可投注
     * LOCKED : 鎖盤
     * CLOSED : 關閉
     * SETTLED : 已結算
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "betting_status", nullable = false, length = 20)
    private SportEventBettingStatus sportEventBettingStatus;

    /**
     * 結算狀態
     * - UNSETTLED：未結算
     * - SETTLED：已結算
     * - VOID：作廢
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "settle_status", nullable = false, length = 20)
    private SportEventSettleStatus sportEventSettleStatus;

    /** 建立時間，自動填入 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間，自動更新 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 新增時自動設定建立與更新時間 */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** 更新時自動刷新更新時間 */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}