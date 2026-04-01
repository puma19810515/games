package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.DepositRequest;
import com.games.dto.WalletResponse;
import com.games.entity.Merchant;
import com.games.entity.User;
import com.games.enums.TransactionType;
import com.games.service.AuthService;
import com.games.service.GamesWalletService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamesWallet")
@RequiredArgsConstructor
@Tag(name = "遊戲錢包", description = "電子遊戲錢包相關 API - 充值、提款等功能")
public class GamesWalletController {

    private final GamesWalletService gamesWalletService;
    private final AuthService authService;

    @Operation(summary = "遊戲錢包充值", description = "向電子遊戲錢包充值指定金額")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "充值成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "充值金額無效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "請求過於頻繁")
    })
    @PostMapping("/deposit")
    @RateLimiter(name = "gamesDeposit")
    public ResponseEntity<ApiResponse<WalletResponse>> gamesDeposit(
            @Valid @RequestBody DepositRequest request,
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        User updatedUser = gamesWalletService.deposit(merchant, user, request.getAmount());

        WalletResponse response = new WalletResponse(
                updatedUser.getUsername(),
                updatedUser.getGameBalance().subtract(request.getAmount()),
                updatedUser.getGameBalance(),
                request.getAmount(),
                TransactionType.DEPOSIT,
                "Deposit successful"
        );
        return ResponseEntity.ok(ApiResponse.success("Deposit successful", response));
    }

    @Operation(summary = "遊戲錢包全額提款", description = "將電子遊戲錢包中的所有餘額提出")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "提款成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "餘額為零"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "請求過於頻繁")
    })
    @PostMapping("/withdraw-all")
    @RateLimiter(name = "gamesWithdraw")
    public ResponseEntity<ApiResponse<WalletResponse>> withdrawAll(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        User updatedUser = gamesWalletService.withdrawAll(merchant, user);

        WalletResponse response = new WalletResponse(
                updatedUser.getUsername(),
                user.getGameBalance(),
                updatedUser.getGameBalance(),
                user.getGameBalance(),
                TransactionType.WITHDRAW,
                "Withdraw all successful"
        );
        return ResponseEntity.ok(ApiResponse.success("Withdraw all successful", response));
    }
}
