package com.games.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 爬蟲聯賽資料請求
 *
 * 用於接收外部爬蟲服務推送的聯賽資料
 * 使用 externalLeagueId 作為唯一識別，支援 Upsert 操作
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "爬蟲聯賽資料請求")
public class CrawlerLeagueRequest {

    /**
     * 外部聯賽ID（爬蟲來源唯一識別）
     * 用於判斷是新增還是更新
     */
    @NotBlank(message = "外部聯賽ID不能為空")
    @Schema(description = "外部聯賽ID，爬蟲來源的唯一識別碼",
            example = "EPL-2024",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String externalLeagueId;

    /**
     * 球種代碼
     * 必須是系統中已存在的球種
     * 可用值：FOOTBALL, BASKETBALL, BASEBALL, TENNIS, ESPORTS 等
     */
    @NotBlank(message = "球種代碼不能為空")
    @Schema(description = "球種代碼，必須是系統已存在的球種",
            example = "FOOTBALL",
            allowableValues = {"FOOTBALL", "BASKETBALL", "BASEBALL", "TENNIS", "ESPORTS"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String sportTypeCode;

    /**
     * 聯賽名稱
     */
    @NotBlank(message = "聯賽名稱不能為空")
    @Schema(description = "聯賽名稱",
            example = "英格蘭超級聯賽",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 國家代碼（ISO 3166-1 alpha-2）
     * 例如：TW-台灣, US-美國, GB-英國, JP-日本
     */
    @Schema(description = "國家代碼（ISO 3166-1 alpha-2）",
            example = "GB",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String countryCode;

    /**
     * 顯示順序
     * 數字越小越靠前，預設為 0
     */
    @Schema(description = "顯示順序，數字越小越靠前",
            example = "1",
            defaultValue = "0",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer displayOrder;

    /**
     * 狀態
     * 0: 停用
     * 1: 啟用（預設）
     */
    @Schema(description = "狀態：0-停用, 1-啟用",
            example = "1",
            defaultValue = "1",
            allowableValues = {"0", "1"},
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer status;
}
