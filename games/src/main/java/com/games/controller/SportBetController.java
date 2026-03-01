package com.games.controller;

import com.games.dto.*;
import com.games.entity.Merchant;
import com.games.entity.User;
import com.games.service.AuthService;
import com.games.service.SportBetService;
import com.games.util.PageDataResUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 體育投注控制器
 *
 * 提供體育投注相關接口：
 * - 下注（單注/串關）
 * - 查詢投注記錄
 * - 查詢投注詳情
 */
@Slf4j
@RestController
@RequestMapping("/api/sport/bet")
@RequiredArgsConstructor
public class SportBetController {

    private final SportBetService sportBetService;
    private final AuthService authService;

    /**
     * 下注接口
     *
     * 支援單注（SINGLE）和串關（PARLAY）投注
     *
     * @param request 投注請求
     * @param authentication 認證資訊
     * @param merchant 商戶資訊（由Filter注入）
     * @return 投注響應
     */
    @PostMapping("/place")
    @RateLimiter(name = "sportBet", fallbackMethod = "placeBetFallback")
    public ResponseEntity<ApiResponse<SportBetResponse>> placeBet(
            @Valid @RequestBody SportBetRequest request,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        try {
            String username = authentication.getName();
            User user = authService.getUserByUsername(merchant.getId(), username);

            SportBetResponse response = sportBetService.placeBet(merchant, user, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("體育投注失敗", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 下注限流降級方法
     */
    public ResponseEntity<ApiResponse<SportBetResponse>> placeBetFallback(
            SportBetRequest request, Authentication authentication, Merchant merchant, Throwable t) {
        log.warn("體育投注限流觸發: {}", t.getMessage());
        return ResponseEntity.status(429)
                .body(ApiResponse.error("系統繁忙，請稍後再試"));
    }

    /**
     * 查詢投注記錄
     *
     * @param request 查詢條件
     * @param authentication 認證資訊
     * @param merchant 商戶資訊
     * @return 投注記錄列表（分頁）
     */
    @PostMapping("/records")
    public ResponseEntity<ApiResponse<PageDataResUtil<SportBetRecordResponse>>> getBetRecords(
            @RequestBody SportBetRecordRequest request,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        try {
            String username = authentication.getName();
            User user = authService.getUserByUsername(merchant.getId(), username);

            PageDataResUtil<SportBetRecordResponse> records = sportBetService.getBetRecords(user, request);
            return ResponseEntity.ok(ApiResponse.success(records));
        } catch (Exception e) {
            log.error("查詢投注記錄失敗", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 查詢單筆投注詳情
     *
     * @param betId 投注單號
     * @param authentication 認證資訊
     * @param merchant 商戶資訊
     * @return 投注詳情
     */
    @GetMapping("/detail/{betId}")
    public ResponseEntity<ApiResponse<SportBetRecordResponse>> getBetDetail(
            @PathVariable Long betId,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        try {
            String username = authentication.getName();
            User user = authService.getUserByUsername(merchant.getId(), username);

            SportBetRecordResponse detail = sportBetService.getBetDetail(user, betId);
            return ResponseEntity.ok(ApiResponse.success(detail));
        } catch (Exception e) {
            log.error("查詢投注詳情失敗", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
