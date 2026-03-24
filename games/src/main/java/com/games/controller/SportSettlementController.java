package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.SettleEventRequest;
import com.games.service.SportSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sport/settlement")
@RequiredArgsConstructor
public class SportSettlementController {

    private final SportSettlementService settlementService;

    /**
     * 結算指定賽事的所有投注
     *
     * @param eventId 賽事ID
     * @param request 結算請求，包含全場及半場比分
     * @return 成功結算的投注數量
     */
    @PostMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<Integer>> settleEvent(
            @PathVariable Long eventId,
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
