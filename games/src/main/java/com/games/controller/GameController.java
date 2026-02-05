package com.games.controller;

import com.games.dto.*;
import com.games.entity.User;
import com.games.service.AuthService;
import com.games.service.SlotGameService;
import com.games.util.PageDataResUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final SlotGameService slotGameService;
    private final AuthService authService;

    @PostMapping("/spin/{gameCode}")
    @RateLimiter(name = "spin", fallbackMethod = "spinFallback")
    public ResponseEntity<ApiResponse<BetResponse>> spin(@PathVariable String gameCode,
                                                         @Valid @RequestBody BetRequest request,
                                                         Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = authService.getUserByUsername(username);

            BetResponse response = slotGameService.placeBet(user, gameCode, request.getAmount());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 限流降级方法
    public ResponseEntity<ApiResponse<BetResponse>> spinFallback(String gameCode, BetRequest request,
                                                                 Authentication authentication, Throwable t) {
        log.info("Rate limit exceeded for spin endpoint: {}", t.getMessage());
        return ResponseEntity.status(429)
                .body(ApiResponse.error("Too many requests. Please try again later."));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBalance(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = authService.getUserByUsername(username);

            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            data.put("balance", user.getBalance());

            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/records")
    public ResponseEntity<ApiResponse<PageDataResUtil<BetRecordsResponse>>> getRecords(Authentication authentication,
                                                                                       @RequestBody BetRecordsRequest request) {
        try {
            String username = authentication.getName();
            User user = authService.getUserByUsername(username);

            PageDataResUtil<BetRecordsResponse> data = slotGameService.getBetRecords(user, request);

            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
