package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.OddsFormatResponse;
import com.games.service.OddsFormatService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/odds-format")
public class OddsFormatController {

    public final OddsFormatService oddsFormatService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<OddsFormatResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(oddsFormatService.findAll()));
    }

    @PostMapping("/updateStatus/{id}/{status}")
    @RateLimiter(name = "oddsFormatUpdateStatus")
    public ResponseEntity<ApiResponse<List<OddsFormatResponse>>> oddsUpdateStatus(
            @PathVariable String id, @PathVariable Integer status) {
        return ResponseEntity.ok(ApiResponse.success(oddsFormatService.updateStatus(id, status)));
    }
}
