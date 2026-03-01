package com.games.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 體育投注請求 DTO
 *
 * 支援單注和串關投注
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportBetRequest {

    /**
     * 投注類型
     * SINGLE - 單注
     * PARLAY - 串關
     */
    @NotBlank(message = "投注類型不能為空")
    @Pattern(regexp = "SINGLE|PARLAY", message = "投注類型必須是 SINGLE 或 PARLAY")
    private String betType;

    /**
     * 投注金額
     */
    @NotNull(message = "投注金額不能為空")
    @DecimalMin(value = "1.00", message = "最小投注金額為 1.00")
    @DecimalMax(value = "1000000.00", message = "最大投注金額為 1000000.00")
    private BigDecimal stake;

    /**
     * 投注明細列表（每一腿）
     */
    @NotNull(message = "投注明細不能為空")
    @NotEmpty(message = "至少需要一個投注明細")
    @Valid
    private List<BetLegRequest> legs;

    /**
     * 單一投注腿請求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BetLegRequest {

        /**
         * 賽事ID
         */
        @NotNull(message = "賽事ID不能為空")
        private Long eventId;

        /**
         * 盤口ID
         */
        @NotNull(message = "盤口ID不能為空")
        private Long marketLineId;

        /**
         * 選擇項：HOME, AWAY, OVER, UNDER, DRAW, YES, NO, ODD, EVEN
         */
        @NotBlank(message = "選擇項不能為空")
        @Pattern(regexp = "HOME|AWAY|OVER|UNDER|DRAW|YES|NO|ODD|EVEN",
                message = "選擇項必須是有效值")
        private String selection;

        /**
         * 波膽比分（僅當玩法為波膽時必填）
         * 格式如：1-0, 2-1
         */
        private String correctScore;
    }
}
