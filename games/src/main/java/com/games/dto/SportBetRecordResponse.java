package com.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 體育投注記錄響應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportBetRecordResponse {

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
     * 實際贏取金額
     */
    private BigDecimal winAmount;

    /**
     * 有效投注額
     */
    private BigDecimal validBet;

    /**
     * 投注狀態
     */
    private String status;

    /**
     * 投注時間
     */
    private LocalDateTime placedAt;

    /**
     * 結算時間
     */
    private LocalDateTime settledAt;

    /**
     * 投注明細列表
     */
    private List<SportBetResponse.BetLegResponse> legs;
}
