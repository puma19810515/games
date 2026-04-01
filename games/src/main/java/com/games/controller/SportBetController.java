package com.games.controller;

import com.games.dto.*;
import com.games.entity.Merchant;
import com.games.entity.User;
import com.games.service.AuthService;
import com.games.service.SportBetService;
import com.games.util.PageDataResUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "體育投注", description = "體育投注相關 API - 包含下注、查詢記錄、取消投注、提前兌現等功能")
public class SportBetController {

    private final SportBetService sportBetService;
    private final AuthService authService;

    @Operation(summary = "下注", description = "支援單注（SINGLE）和串關（PARLAY）投注")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "投注成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "投注失敗"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權")
    })
    @PostMapping("/place")
    @RateLimiter(name = "sportBet")
    public ResponseEntity<ApiResponse<SportBetResponse>> placeBet(
            @Valid @RequestBody SportBetRequest request,
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        SportBetResponse response = sportBetService.placeBet(merchant, user, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "查詢投注記錄", description = "分頁查詢用戶投注記錄，支援按類型、狀態、時間篩選")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權")
    })
    @PostMapping("/records")
    public ResponseEntity<ApiResponse<PageDataResUtil<SportBetRecordResponse>>> getBetRecords(
            @RequestBody SportBetRecordRequest request,
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        PageDataResUtil<SportBetRecordResponse> records = sportBetService.getBetRecords(user, request);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @Operation(summary = "查詢單筆投注詳情", description = "根據投注單ID查詢詳細資訊")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "投注單不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權")
    })
    @GetMapping("/detail/{betId}")
    public ResponseEntity<ApiResponse<SportBetRecordResponse>> getBetDetail(
            @Parameter(description = "投注單ID") @PathVariable Long betId,
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        SportBetRecordResponse detail = sportBetService.getBetDetail(user, betId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    // ==================== 取消投注 ====================

    @Operation(summary = "取消投注", description = """
            取消投注限制：
            - 僅允許下注後 5 分鐘內取消
            - 賽事尚未開始
            - 投注狀態為待結算
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "取消成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "取消失敗"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權")
    })
    @PostMapping("/cancel")
    @RateLimiter(name = "sportBet")
    public ResponseEntity<ApiResponse<CancelBetResponse>> cancelBet(
            @Valid @RequestBody CancelBetRequest request,
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
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

    @Operation(summary = "查詢提前兌現報價", description = "返回當前可兌現金額，報價有效期 30 秒")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "不可兌現"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權")
    })
    @GetMapping("/cashout/quote/{betId}")
    public ResponseEntity<ApiResponse<CashoutQuoteResponse>> getCashoutQuote(
            @Parameter(description = "投注單ID") @PathVariable Long betId,
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
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

    @Operation(summary = "執行提前兌現", description = """
            提前兌現限制：
            - 投注狀態為待結算
            - 賽事尚未結束
            - 賽事未取消或延期
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "兌現成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "兌現失敗"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權")
    })
    @PostMapping("/cashout")
    @RateLimiter(name = "sportBet")
    public ResponseEntity<ApiResponse<CashoutResponse>> cashout(
            @Valid @RequestBody CashoutRequest request,
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
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
