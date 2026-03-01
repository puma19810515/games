package com.games.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 體育投注記錄查詢請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportBetRecordRequest {

    /**
     * 頁碼（從0開始）
     */
    @Min(value = 0, message = "頁碼不能小於0")
    private Integer page = 0;

    /**
     * 每頁大小
     */
    @Min(value = 1, message = "每頁大小不能小於1")
    private Integer size = 10;

    /**
     * 投注類型過濾：SINGLE, PARLAY
     */
    private String betType;

    /**
     * 狀態過濾：PENDING, SETTLED, CANCELLED, CASHED_OUT
     */
    private String status;

    /**
     * 球種代碼過濾
     */
    private String sportTypeCode;

    /**
     * 開始時間
     */
    private LocalDateTime startTime;

    /**
     * 結束時間
     */
    private LocalDateTime endTime;
}
