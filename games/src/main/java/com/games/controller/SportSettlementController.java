package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.SettleEventRequest;
import com.games.service.SportSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sport/settlement")
@RequiredArgsConstructor
@Tag(name = "體育結算", description = "體育賽事結算相關 API")
public class SportSettlementController {

    private final SportSettlementService settlementService;

    @Operation(summary = "結算賽事", description = """
            結算指定賽事的所有相關投注
            
            支援的玩法包括：
            - 亞洲讓球 (AH)
            - 大小球 (OU)
            - 獨贏 (1X2)
            - 波膽 (CS)
            - 單雙 (OE)
            - 雙方進球 (BTTS)
            - 美國盤線 (US_*)
            
            串關投注會根據各腿結果計算最終派彩
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "結算成功，返回結算的投注數量"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "結算失敗"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "賽事不存在")
    })
    @PostMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<Integer>> settleEvent(
            @Parameter(description = "賽事ID") @PathVariable Long eventId,
            @RequestBody SettleEventRequest request) {
        int count = settlementService.settleEventBets(
                eventId,
                request.getHomeScore(),
                request.getAwayScore(),
                request.getHomeScoreHalf(),
                request.getAwayScoreHalf()
        );
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
