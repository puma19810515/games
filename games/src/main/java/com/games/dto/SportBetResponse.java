package com.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 體育投注響應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportBetResponse {

    /**
     * 投注單號
     */
    private Long betId;

    /**
     * 投注類型：SINGLE-單注, PARLAY-串關
     */
    private String betType;

    /**
     * 投注金額
     */
    private BigDecimal stake;

    /**
     * 總賠率
     */
    private BigDecimal totalOdds;

    /**
     * 預計贏取金額
     */
    private BigDecimal potentialWin;

    /**
     * 投注狀態
     */
    private String status;

    /**
     * 投注時間
     */
    private LocalDateTime placedAt;

    /**
     * 投注後餘額
     */
    private BigDecimal balanceAfter;

    /**
     * 投注明細列表
     */
    private List<BetLegResponse> legs;

    /**
     * 單一投注腿響應
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BetLegResponse {

        /**
         * 投注腿ID
         */
        private Long legId;

        /**
         * 賽事ID
         */
        private Long eventId;

        /**
         * 賽事名稱（主隊 vs 客隊）
         */
        private String eventName;

        /**
         * 聯賽名稱
         */
        private String leagueName;

        /**
         * 球種名稱
         */
        private String sportTypeName;

        /**
         * 開賽時間
         */
        private LocalDateTime startTime;

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
         * 選擇項
         */
        private String selection;

        /**
         * 選擇項顯示文字
         */
        private String selectionDisplay;

        /**
         * 讓球/大小值
         */
        private BigDecimal handicap;

        /**
         * 原始賠率
         */
        private BigDecimal odds;

        /**
         * 歐洲盤賠率（用於計算）
         */
        private BigDecimal oddsDecimal;

        /**
         * 結果
         */
        private String result;
    }
}
