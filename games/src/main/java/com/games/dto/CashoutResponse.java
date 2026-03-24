package com.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提前兌現響應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashoutResponse {

    /**
     * 投注單ID
     */
    private Long betId;

    /**
     * 原投注金額
     */
    private BigDecimal originalStake;

    /**
     * 預計最大贏取金額
     */
    private BigDecimal potentialWin;

    /**
     * 兌現金額
     */
    private BigDecimal cashoutAmount;

    /**
     * 兌現盈虧（兌現金額 - 原投注金額）
     */
    private BigDecimal profitLoss;

    /**
     * 兌現後餘額
     */
    private BigDecimal balanceAfter;

    /**
     * 兌現時間
     */
    private LocalDateTime cashedOutAt;

    /**
     * 狀態
     */
    private String status;
}
