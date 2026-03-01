package com.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 賽事響應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportEventResponse {

    /**
     * 賽事ID
     */
    private Long eventId;

    /**
     * 球種代碼
     */
    private String sportTypeCode;

    /**
     * 球種名稱
     */
    private String sportTypeName;

    /**
     * 聯賽ID
     */
    private Long leagueId;

    /**
     * 聯賽名稱
     */
    private String leagueName;

    /**
     * 主隊ID
     */
    private Long homeTeamId;

    /**
     * 主隊名稱
     */
    private String homeTeamName;

    /**
     * 客隊ID
     */
    private Long awayTeamId;

    /**
     * 客隊名稱
     */
    private String awayTeamName;

    /**
     * 開賽時間
     */
    private LocalDateTime startTime;

    /**
     * 主隊比分
     */
    private Integer homeScore;

    /**
     * 客隊比分
     */
    private Integer awayScore;

    /**
     * 賽事狀態：UPCOMING, LIVE, FINISHED, CANCELLED, POSTPONED
     */
    private String status;

    /**
     * 投注狀態：OPEN, LOCKED, CLOSED, SETTLED
     */
    private String bettingStatus;

    /**
     * 盤口列表
     */
    private List<MarketLineResponse> marketLines;

    /**
     * 盤口響應
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketLineResponse {

        /**
         * 盤口ID
         */
        private Long marketLineId;

        /**
         * 玩法代碼
         */
        private String betTypeCode;

        /**
         * 玩法名稱
         */
        private String betTypeName;

        /**
         * 賠率格式代碼
         */
        private String oddsFormatCode;

        /**
         * 賠率格式名稱
         */
        private String oddsFormatName;

        /**
         * 讓球/大小值
         */
        private BigDecimal handicap;

        /**
         * 主隊賠率
         */
        private BigDecimal homeOdds;

        /**
         * 客隊賠率
         */
        private BigDecimal awayOdds;

        /**
         * 和局賠率
         */
        private BigDecimal drawOdds;

        /**
         * 大盤賠率
         */
        private BigDecimal overOdds;

        /**
         * 小盤賠率
         */
        private BigDecimal underOdds;

        /**
         * 是賠率
         */
        private BigDecimal yesOdds;

        /**
         * 否賠率
         */
        private BigDecimal noOdds;

        /**
         * 單賠率
         */
        private BigDecimal oddOdds;

        /**
         * 雙賠率
         */
        private BigDecimal evenOdds;

        /**
         * 波膽賠率
         */
        private Map<String, Object> scoreOdds;
    }
}
