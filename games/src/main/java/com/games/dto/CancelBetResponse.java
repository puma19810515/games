package com.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投注取消響應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelBetResponse {

    /**
     * 投注單ID
     */
    private Long betId;

    /**
     * 退還金額
     */
    private BigDecimal refundAmount;

    /**
     * 退還後餘額
     */
    private BigDecimal balanceAfter;

    /**
     * 取消時間
     */
    private LocalDateTime cancelledAt;

    /**
     * 取消原因
     */
    private String reason;

    /**
     * 狀態
     */
    private String status;
}
