package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.BetResponse;
import com.games.dto.OddsFormatResponse;
import com.games.service.OddsFormatService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/odds-format")
public class OddsFormatController {

    public final OddsFormatService oddsFormatService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<OddsFormatResponse>>> list() {
        try {
            return ResponseEntity.ok(ApiResponse.success(oddsFormatService.findAll()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/updateStatus/{id}/{status}")
    @RateLimiter(name = "oddsFormatUpdateStatus", fallbackMethod = "oddsFormatUpdateStatusFailback")
    public ResponseEntity<ApiResponse<List<OddsFormatResponse>>> oddsUpdateStatus(
            @PathVariable String id, @PathVariable Integer status) {
        try {
            return ResponseEntity.ok(ApiResponse.success(oddsFormatService.updateStatus(id, status)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 限流降级方法
    public ResponseEntity<ApiResponse<BetResponse>> oddsFormatUpdateStatusFailback(
            String id, Integer status, Throwable t) {
        log.info("Rate limit exceeded for updateStatus endpoint: {}", t.getMessage());
        return ResponseEntity.status(429)
                .body(ApiResponse.error("Too many requests. Please try again later."));
    }
}
