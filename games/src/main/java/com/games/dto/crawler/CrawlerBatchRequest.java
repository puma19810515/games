package com.games.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 爬蟲批量資料請求（支援單個或批量）
 *
 * 泛型包裝器，用於批量推送資料
 *
 * @param <T> 資料類型，可以是 CrawlerLeagueRequest, CrawlerTeamRequest 等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "爬蟲批量資料請求包裝器")
public class CrawlerBatchRequest<T> {

    /**
     * 資料列表
     * 支援批量推送，單次最多建議 1000 筆
     */
    @NotEmpty(message = "資料列表不能為空")
    @Valid
    @Schema(description = "資料列表，支援批量推送",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<T> items;

    /**
     * 來源標識
     * 用於追蹤資料來自哪個爬蟲服務
     */
    @Schema(description = "資料來源標識，用於追蹤",
            example = "ODDS-CRAWLER-01",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String source;

    /**
     * 批次ID
     * 用於追蹤和排查問題
     * 如果未提供，系統會自動生成
     */
    @Schema(description = "批次ID，用於追蹤。未提供時系統自動生成",
            example = "BATCH-20260401-001",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String batchId;
}
