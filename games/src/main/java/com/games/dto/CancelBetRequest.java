package com.games.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 投注取消請求 DTO
 */
@Data
public class CancelBetRequest {

    /**
     * 投注單ID
     */
    @NotNull(message = "投注單ID不能為空")
    private Long betId;

    /**
     * 取消原因
     */
    private String reason;
}
