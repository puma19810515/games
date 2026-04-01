package com.games.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.games.dto.*;
import com.games.entity.Merchant;
import com.games.entity.User;
import com.games.service.AuthService;
import com.games.service.SlotGameService;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@Tag(name = "電子遊戲", description = "電子遊戲相關 API - 包含老虎機遊戲、餘額查詢、投注記錄等功能")
public class GameController {

    private final SlotGameService slotGameService;
    private final AuthService authService;

    @Operation(summary = "老虎機下注", description = "對指定遊戲進行下注，會根據 RTP 設定計算中獎結果")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "投注成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "餘額不足或參數錯誤"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "請求過於頻繁")
    })
    @PostMapping("/spin/{gameCode}")
    @RateLimiter(name = "spin")
    public ResponseEntity<ApiResponse<BetResponse>> spin(
            @Parameter(description = "遊戲代碼，例如：FRUIT_SLOT") @PathVariable String gameCode,
            @Valid @RequestBody BetRequest request,
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant)
            throws JsonProcessingException {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        BetResponse response = slotGameService.placeBet(merchant, user, gameCode, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "查詢餘額", description = "查詢當前用戶的遊戲餘額和體育餘額")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權")
    })
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBalance(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);

        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("gameBalance", user.getGameBalance());
        data.put("sportBalance", user.getSportBalance());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @Operation(summary = "查詢投注記錄", description = "分頁查詢電子遊戲投注記錄")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權")
    })
    @PostMapping("/records")
    public ResponseEntity<ApiResponse<PageDataResUtil<BetRecordsResponse>>> getRecords(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody BetRecordsRequest request,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        PageDataResUtil<BetRecordsResponse> data = slotGameService.getBetRecords(user, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
