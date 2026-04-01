package com.games.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 爬蟲賽事資料請求
 *
 * 用於接收外部爬蟲服務推送的賽事資料
 * 使用 externalEventId 作為唯一識別，支援 Upsert 操作
 *
 * 依賴關係：
 * - 需要先推送隊伍資料（externalHomeTeamId, externalAwayTeamId 必須已存在）
 * - 聯賽資料可選（externalLeagueId 如提供則必須已存在）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "爬蟲賽事資料請求")
public class CrawlerEventRequest {

    /**
     * 外部賽事ID（爬蟲來源唯一識別）
     * 用於判斷是新增還是更新
     */
    @NotBlank(message = "外部賽事ID不能為空")
    @Schema(description = "外部賽事ID，爬蟲來源的唯一識別碼",
            example = "MATCH-20260401-001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String externalEventId;

    /**
     * 球種代碼
     */
    @NotBlank(message = "球種代碼不能為空")
    @Schema(description = "球種代碼",
            example = "FOOTBALL",
            allowableValues = {"FOOTBALL", "BASKETBALL", "BASEBALL", "TENNIS", "ESPORTS"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String sportTypeCode;

    /**
     * 外部聯賽ID
     * 如果提供，必須是已存在的聯賽
     */
    @Schema(description = "外部聯賽ID，必須是已推送過的聯賽",
            example = "EPL-2024",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String externalLeagueId;

    /**
     * 外部主隊ID
     * 必須是已存在的隊伍
     */
    @NotBlank(message = "外部主隊ID不能為空")
    @Schema(description = "外部主隊ID，必須是已推送過的隊伍",
            example = "MUN-001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String externalHomeTeamId;

    /**
     * 外部客隊ID
     * 必須是已存在的隊伍
     */
    @NotBlank(message = "外部客隊ID不能為空")
    @Schema(description = "外部客隊ID，必須是已推送過的隊伍",
            example = "MCI-001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String externalAwayTeamId;

    /**
     * 主隊名稱（冗餘欄位）
     * 用於快速顯示，避免關聯查詢
     */
    @NotBlank(message = "主隊名稱不能為空")
    @Schema(description = "主隊名稱（冗餘欄位，用於快速顯示）",
            example = "曼徹斯特聯",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String homeTeamName;

    /**
     * 客隊名稱（冗餘欄位）
     */
    @NotBlank(message = "客隊名稱不能為空")
    @Schema(description = "客隊名稱（冗餘欄位，用於快速顯示）",
            example = "曼徹斯特城",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String awayTeamName;

    /**
     * 開賽時間
     * 格式：ISO 8601（例如 2026-04-01T15:00:00）
     */
    @NotNull(message = "開賽時間不能為空")
    @Schema(description = "開賽時間（ISO 8601 格式）",
            example = "2026-04-01T15:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    /**
     * 全場主隊比分
     * 比賽進行中或結束後更新
     */
    @Schema(description = "全場主隊比分",
            example = "2",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer homeScore;

    /**
     * 全場客隊比分
     */
    @Schema(description = "全場客隊比分",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer awayScore;

    /**
     * 半場主隊比分
     */
    @Schema(description = "半場主隊比分",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer homeScoreHalf;

    /**
     * 半場客隊比分
     */
    @Schema(description = "半場客隊比分",
            example = "0",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer awayScoreHalf;

    /**
     * 賽事狀態
     * - UPCOMING: 未開賽
     * - LIVE: 進行中
     * - FINISHED: 已結束
     * - CANCELLED: 取消
     * - POSTPONED: 延期
     */
    @Schema(description = "賽事狀態",
            example = "UPCOMING",
            defaultValue = "UPCOMING",
            allowableValues = {"UPCOMING", "LIVE", "FINISHED", "CANCELLED", "POSTPONED"},
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String status;

    /**
     * 投注狀態
     * - OPEN: 可投注
     * - LOCKED: 鎖盤（暫停投注）
     * - CLOSED: 關閉
     * - SETTLED: 已結算
     */
    @Schema(description = "投注狀態",
            example = "OPEN",
            defaultValue = "OPEN",
            allowableValues = {"OPEN", "LOCKED", "CLOSED", "SETTLED"},
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String bettingStatus;
}
