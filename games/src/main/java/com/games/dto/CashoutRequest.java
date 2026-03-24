package com.games.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提前兌現請求 DTO
 */
@Data
public class CashoutRequest {

    /**
     * 投注單ID
     */
    @NotNull(message = "投注單ID不能為空")
    private Long betId;

    /**
     * 用戶確認的兌現金額（可選，用於確認金額是否一致）
     * 如果不提供，則使用系統計算的金額
     */
    private BigDecimal confirmedAmount;
}
