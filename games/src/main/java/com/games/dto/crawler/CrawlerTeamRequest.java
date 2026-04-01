package com.games.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 爬蟲隊伍資料請求
 *
 * 用於接收外部爬蟲服務推送的隊伍資料
 * 使用 externalTeamId 作為唯一識別，支援 Upsert 操作
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "爬蟲隊伍資料請求")
public class CrawlerTeamRequest {

    /**
     * 外部隊伍ID（爬蟲來源唯一識別）
     * 用於判斷是新增還是更新
     */
    @NotBlank(message = "外部隊伍ID不能為空")
    @Schema(description = "外部隊伍ID，爬蟲來源的唯一識別碼",
            example = "MUN-001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String externalTeamId;

    /**
     * 球種代碼
     * 必須是系統中已存在的球種
     */
    @NotBlank(message = "球種代碼不能為空")
    @Schema(description = "球種代碼，必須是系統已存在的球種",
            example = "FOOTBALL",
            allowableValues = {"FOOTBALL", "BASKETBALL", "BASEBALL", "TENNIS", "ESPORTS"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String sportTypeCode;

    /**
     * 隊伍全名
     */
    @NotBlank(message = "隊伍名稱不能為空")
    @Schema(description = "隊伍全名",
            example = "曼徹斯特聯",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 隊伍簡稱
     * 通常為 2-4 個字
     */
    @Schema(description = "隊伍簡稱，通常為 2-4 個字",
            example = "曼聯",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String shortName;

    /**
     * 隊徽圖片URL
     * 建議使用 HTTPS 協議
     */
    @Schema(description = "隊徽圖片URL",
            example = "https://example.com/logos/manchester-united.png",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String logoUrl;
}
