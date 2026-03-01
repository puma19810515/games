package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.OddsFormatResponse;
import com.games.dto.SportTypeResponse;
import com.games.service.SportTypeService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sport-type")
public class SportTypeController {

    public final SportTypeService sportTypeService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SportTypeResponse>>> list() {
        try {
            return ResponseEntity.ok(ApiResponse.success(sportTypeService.findAll()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/updateStatus/{id}/{status}")
    @RateLimiter(name = "sportTypeUpdateStatus", fallbackMethod = "sportTypeUpdateStatusFailback")
    public ResponseEntity<ApiResponse<List<SportTypeResponse>>> oddsUpdateStatus(
            @PathVariable String id, @PathVariable Integer status) {
        try {
            return ResponseEntity.ok(ApiResponse.success(sportTypeService.updateStatus(id, status)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
