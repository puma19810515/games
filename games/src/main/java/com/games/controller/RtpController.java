package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.service.RtpStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/rtp")
@RequiredArgsConstructor
@Tag(name = "RTP 統計", description = "遊戲 RTP（返還率）統計相關 API")
public class RtpController {

    private final RtpStatisticsService rtpStatisticsService;

    @Operation(summary = "查詢 RTP 統計", description = "查詢指定遊戲的 RTP 統計數據")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功")
    })
    @GetMapping("/statistics/{gameCode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @Parameter(description = "遊戲代碼") @PathVariable String gameCode) {
        Map<String, Object> stats = rtpStatisticsService.getStatistics(gameCode);
        return ResponseEntity.ok(ApiResponse.success("RTP statistics retrieved successfully", stats));
    }

    @Operation(summary = "重置 RTP 統計", description = "重置指定遊戲的 RTP 統計數據")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "重置成功")
    })
    @PostMapping("/reset/{gameCode}")
    public ResponseEntity<ApiResponse<String>> resetStatistics(
            @Parameter(description = "遊戲代碼") @PathVariable String gameCode) {
        rtpStatisticsService.resetStatistics(gameCode);
        return ResponseEntity.ok(ApiResponse.success("RTP statistics reset successfully", null));
    }
}
