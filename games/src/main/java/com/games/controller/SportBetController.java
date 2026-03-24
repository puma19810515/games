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

@RestController
@RequestMapping("/api/sport/bet")
@RequiredArgsConstructor
@Slf4j
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
    @RateLimiter(name = "sportBet")
    public ResponseEntity<ApiResponse<SportBetResponse>> placeBet(
            @Valid @RequestBody SportBetRequest request,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        SportBetResponse response = sportBetService.placeBet(merchant, user, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 查詢投注記錄
     */
    @PostMapping("/records")
    public ResponseEntity<ApiResponse<PageDataResUtil<SportBetRecordResponse>>> getBetRecords(
            @RequestBody SportBetRecordRequest request,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        PageDataResUtil<SportBetRecordResponse> records = sportBetService.getBetRecords(user, request);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * 查詢單筆投注詳情
     */
    @GetMapping("/detail/{betId}")
    public ResponseEntity<ApiResponse<SportBetRecordResponse>> getBetDetail(
            @PathVariable Long betId,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        SportBetRecordResponse detail = sportBetService.getBetDetail(user, betId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    // ==================== 取消投注 ====================

    /**
     * 取消投注
     *
     * 限制：
     * - 僅允許下注後 5 分鐘內取消
     * - 賽事尚未開始
     * - 投注狀態為待結算
     *
     * @param request 取消請求
     * @param authentication 認證資訊
     * @param merchant 商戶資訊
     * @return 取消響應
     */
    @PostMapping("/cancel")
    @RateLimiter(name = "sportBet")
    public ResponseEntity<ApiResponse<CancelBetResponse>> cancelBet(
            @Valid @RequestBody CancelBetRequest request,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        try {
            String username = authentication.getName();
            User user = authService.getUserByUsername(merchant.getId(), username);
            CancelBetResponse response = sportBetService.cancelBet(merchant, user, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("取消投注失敗", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== 提前兌現（Cashout） ====================

    /**
     * 查詢提前兌現報價
     *
     * 返回當前可兌現金額，報價有效期 30 秒
     *
     * @param betId 投注單ID
     * @param authentication 認證資訊
     * @param merchant 商戶資訊
     * @return 兌現報價
     */
    @GetMapping("/cashout/quote/{betId}")
    public ResponseEntity<ApiResponse<CashoutQuoteResponse>> getCashoutQuote(
            @PathVariable Long betId,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        try {
            String username = authentication.getName();
            User user = authService.getUserByUsername(merchant.getId(), username);
            CashoutQuoteResponse quote = sportBetService.getCashoutQuote(user, betId);
            return ResponseEntity.ok(ApiResponse.success(quote));
        } catch (Exception e) {
            log.error("查詢兌現報價失敗", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 執行提前兌現
     *
     * 限制：
     * - 投注狀態為待結算
     * - 賽事尚未結束
     * - 賽事未取消或延期
     *
     * @param request 兌現請求
     * @param authentication 認證資訊
     * @param merchant 商戶資訊
     * @return 兌現響應
     */
    @PostMapping("/cashout")
    @RateLimiter(name = "sportBet")
    public ResponseEntity<ApiResponse<CashoutResponse>> cashout(
            @Valid @RequestBody CashoutRequest request,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        try {
            String username = authentication.getName();
            User user = authService.getUserByUsername(merchant.getId(), username);
            CashoutResponse response = sportBetService.cashout(merchant, user, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("提前兌現失敗", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
