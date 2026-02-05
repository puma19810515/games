package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.service.RtpStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/rtp")
@RequiredArgsConstructor
public class RtpController {

    private final RtpStatisticsService rtpStatisticsService;

    @GetMapping("/statistics/{gameCode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(@PathVariable String gameCode) {
        try {
            Map<String, Object> stats = rtpStatisticsService.getStatistics(gameCode);
            return ResponseEntity.ok(ApiResponse.success("RTP statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset/{gameCode}")
    public ResponseEntity<ApiResponse<String>> resetStatistics(@PathVariable String gameCode) {
        try {
            rtpStatisticsService.resetStatistics(gameCode);
            return ResponseEntity.ok(ApiResponse.success("RTP statistics reset successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
