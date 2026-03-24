package com.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 提前兌現報價響應 DTO
 *
 * 用於查詢可兌現金額，不執行實際兌現操作
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashoutQuoteResponse {

    /**
     * 投注單ID
     */
    private Long betId;

    /**
     * 是否可兌現
     */
    private boolean cashoutAvailable;

    /**
     * 不可兌現原因（如果不可兌現）
     */
    private String unavailableReason;

    /**
     * 原投注金額
     */
    private BigDecimal originalStake;

    /**
     * 預計最大贏取金額
     */
    private BigDecimal potentialWin;

    /**
     * 可兌現金額
     */
    private BigDecimal cashoutAmount;

    /**
     * 兌現盈虧（兌現金額 - 原投注金額）
     */
    private BigDecimal profitLoss;

    /**
     * 兌現比例（兌現金額 / 預計最大贏取金額）
     */
    private BigDecimal cashoutPercentage;

    /**
     * 報價有效期（秒）
     */
    private int validForSeconds;
}
