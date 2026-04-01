package com.games.dto.crawler;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 爬蟲盤口賠率請求
 *
 * 用於接收外部爬蟲服務推送的盤口賠率資料
 * 使用 externalMarketId 作為唯一識別，支援 Upsert 操作
 *
 * 依賴關係：
 * - 需要先推送賽事資料（externalEventId 必須已存在）
 * - 玩法類型（betTypeCode）必須是系統預設的玩法
 * - 賠率格式（oddsFormatCode）必須是系統預設的格式
 *
 * 根據不同玩法類型，填入對應的賠率欄位：
 * - 讓球盤 (AH): homeOdds, awayOdds, handicap
 * - 大小盤 (OU): overOdds, underOdds, handicap
 * - 獨贏盤 (1X2): homeOdds, drawOdds, awayOdds
 * - 兩隊進球 (BTTS): yesOdds, noOdds
 * - 單雙 (OE): oddOdds, evenOdds
 * - 波膽 (CS): scoreOdds (JSON)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "爬蟲盤口賠率請求")
public class CrawlerMarketLineRequest {

    /**
     * 外部盤口ID（爬蟲來源唯一識別）
     * 用於判斷是新增還是更新
     */
    @NotBlank(message = "外部盤口ID不能為空")
    @Schema(description = "外部盤口ID，爬蟲來源的唯一識別碼",
            example = "ML-20260401-001-AH-0.5",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String externalMarketId;

    /**
     * 外部賽事ID
     * 必須是已存在的賽事
     */
    @NotBlank(message = "外部賽事ID不能為空")
    @Schema(description = "外部賽事ID，必須是已推送過的賽事",
            example = "MATCH-20260401-001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String externalEventId;

    /**
     * 玩法類型代碼
     * 必須是系統已存在的玩法
     */
    @NotBlank(message = "玩法代碼不能為空")
    @Schema(description = "玩法類型代碼",
            example = "AH",
            allowableValues = {"AH", "OU", "1X2", "CS", "BTTS", "OE",
                              "AH_HALF", "OU_HALF", "1X2_HALF",
                              "MY_AH", "HK_AH", "US_ML"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String betTypeCode;

    /**
     * 賠率格式代碼
     * 必須是系統已存在的格式
     */
    @NotBlank(message = "賠率格式代碼不能為空")
    @Schema(description = "賠率格式代碼",
            example = "ASIAN",
            allowableValues = {"ASIAN", "EUROPEAN", "HONGKONG", "MALAY", "INDO", "AMERICAN", "INDIAN"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String oddsFormatCode;

    /**
     * 讓球/大小值
     * 讓球盤：負數表示讓球，正數表示受讓（如 -0.5, +1.5）
     * 大小盤：總分線（如 2.5, 3.0）
     */
    @Schema(description = "讓球/大小值。讓球盤：-0.5 表示讓半球；大小盤：2.5 表示總分線",
            example = "-0.5",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal handicap;

    /**
     * 主隊賠率
     * 用於：讓球盤(AH)、獨贏盤(1X2)
     */
    @Schema(description = "主隊賠率，用於讓球盤和獨贏盤",
            example = "1.85",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal homeOdds;

    /**
     * 客隊賠率
     * 用於：讓球盤(AH)、獨贏盤(1X2)
     */
    @Schema(description = "客隊賠率，用於讓球盤和獨贏盤",
            example = "2.05",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal awayOdds;

    /**
     * 和局賠率
     * 用於：獨贏盤(1X2)
     */
    @Schema(description = "和局賠率，僅用於獨贏盤(1X2)",
            example = "3.40",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal drawOdds;

    /**
     * 大盤賠率
     * 用於：大小盤(OU)
     */
    @Schema(description = "大盤賠率，用於大小盤",
            example = "1.90",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal overOdds;

    /**
     * 小盤賠率
     * 用於：大小盤(OU)
     */
    @Schema(description = "小盤賠率，用於大小盤",
            example = "1.95",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal underOdds;

    /**
     * 是賠率
     * 用於：兩隊進球(BTTS)
     */
    @Schema(description = "「是」賠率，用於兩隊進球等玩法",
            example = "1.75",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal yesOdds;

    /**
     * 否賠率
     * 用於：兩隊進球(BTTS)
     */
    @Schema(description = "「否」賠率，用於兩隊進球等玩法",
            example = "2.10",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal noOdds;

    /**
     * 單數賠率
     * 用於：單雙盤(OE)
     */
    @Schema(description = "單數賠率，用於單雙盤",
            example = "1.90",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal oddOdds;

    /**
     * 雙數賠率
     * 用於：單雙盤(OE)
     */
    @Schema(description = "雙數賠率，用於單雙盤",
            example = "1.90",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal evenOdds;

    /**
     * 波膽賠率（JSON格式）
     * 用於：波膽盤(CS)
     * 格式：{"比分": 賠率}
     * 例如：{"1-0": 7.50, "2-1": 9.50, "0-0": 11.00}
     */
    @Schema(description = "波膽賠率（JSON格式），格式為 {\"比分\": 賠率}",
            example = "{\"1-0\": 7.50, \"2-1\": 9.50, \"0-0\": 11.00}",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Map<String, Object> scoreOdds;

    /**
     * 是否有效
     * true: 有效（可投注）
     * false: 停用（不可投注）
     */
    @Schema(description = "是否有效：true-可投注, false-停用",
            example = "true",
            defaultValue = "true",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isActive;
}
