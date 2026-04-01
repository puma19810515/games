package com.games.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 爬蟲資料處理響應
 *
 * 統一的響應格式，包含處理統計和失敗詳情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "爬蟲資料處理響應")
public class CrawlerResponse {

    /**
     * 處理成功數量（新增 + 更新）
     */
    @Schema(description = "處理成功數量（新增 + 更新）", example = "10")
    private int successCount;

    /**
     * 更新數量（已存在的資料被更新）
     */
    @Schema(description = "更新數量（已存在被更新）", example = "3")
    private int updatedCount;

    /**
     * 新增數量（新資料被插入）
     */
    @Schema(description = "新增數量", example = "7")
    private int insertedCount;

    /**
     * 失敗數量
     */
    @Schema(description = "失敗數量", example = "0")
    private int failedCount;

    /**
     * 總處理數量
     */
    @Schema(description = "總處理數量", example = "10")
    private int totalCount;

    /**
     * 失敗的項目列表
     * 包含失敗的外部ID和錯誤訊息
     */
    @Builder.Default
    @Schema(description = "失敗項目列表，包含失敗原因")
    private List<FailedItem> failedItems = new ArrayList<>();

    /**
     * 處理時間（毫秒）
     */
    @Schema(description = "處理時間（毫秒）", example = "125")
    private long processingTimeMs;

    /**
     * 批次ID
     * 用於追蹤和排查問題
     */
    @Schema(description = "批次ID，用於追蹤", example = "BATCH-A1B2C3D4")
    private String batchId;

    /**
     * 處理完成時間
     */
    @Schema(description = "處理完成時間", example = "2026-04-01T10:30:00")
    private LocalDateTime processedAt;

    /**
     * 失敗項目詳情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "失敗項目詳情")
    public static class FailedItem {

        /**
         * 失敗的外部ID
         */
        @Schema(description = "失敗的外部ID", example = "INVALID-001")
        private String externalId;

        /**
         * 錯誤訊息
         */
        @Schema(description = "錯誤訊息", example = "球種不存在: UNKNOWN_SPORT")
        private String errorMessage;
    }

    /**
     * 建立成功響應的快捷方法
     *
     * @param inserted 新增數量
     * @param updated 更新數量
     * @param timeMs 處理時間（毫秒）
     * @return CrawlerResponse
     */
    public static CrawlerResponse success(int inserted, int updated, long timeMs) {
        return CrawlerResponse.builder()
                .successCount(inserted + updated)
                .insertedCount(inserted)
                .updatedCount(updated)
                .failedCount(0)
                .totalCount(inserted + updated)
                .processingTimeMs(timeMs)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
