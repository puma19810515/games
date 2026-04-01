package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.DepositRequest;
import com.games.dto.SportWalletResponse;
import com.games.dto.WalletResponse;
import com.games.entity.Merchant;
import com.games.entity.User;
import com.games.enums.SportTransactionType;
import com.games.enums.TransactionType;
import com.games.service.AuthService;
import com.games.service.SportWalletService;
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
@RequestMapping("/api/sportWallet")
@RequiredArgsConstructor
@Tag(name = "體育錢包", description = "體育錢包相關 API - 充值、提款等功能")
public class SportWalletController {

    private final SportWalletService sportsWalletService;
    private final AuthService authService;

    @Operation(summary = "體育錢包充值", description = "向體育錢包充值指定金額")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "充值成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "充值金額無效"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "請求過於頻繁")
    })
    @PostMapping("/deposit")
    @RateLimiter(name = "sportDeposit")
    public ResponseEntity<ApiResponse<SportWalletResponse>> sportDeposit(
            @Valid @RequestBody DepositRequest request,
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        User updatedUser = sportsWalletService.deposit(merchant, user, request.getAmount());

        SportWalletResponse response = new SportWalletResponse(
                updatedUser.getUsername(),
                updatedUser.getSportBalance().subtract(request.getAmount()),
                updatedUser.getSportBalance(),
                request.getAmount(),
                SportTransactionType.SPORT_DEPOSIT,
                "Sport Deposit successful"
        );
        return ResponseEntity.ok(ApiResponse.success("Sport Deposit successful", response));
    }

    @Operation(summary = "體育錢包全額提款", description = "將體育錢包中的所有餘額提出")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "提款成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "餘額為零"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "請求過於頻繁")
    })
    @PostMapping("/withdraw-all")
    @RateLimiter(name = "sportWithdraw")
    public ResponseEntity<ApiResponse<SportWalletResponse>> withdrawAll(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        User updatedUser = sportsWalletService.withdrawAll(merchant, user);

        SportWalletResponse response = new SportWalletResponse(
                updatedUser.getUsername(),
                user.getSportBalance(),
                updatedUser.getSportBalance(),
                user.getSportBalance(),
                SportTransactionType.SPORT_WITHDRAW,
                "Sport withdraw all successful"
        );
        return ResponseEntity.ok(ApiResponse.success("Sport withdraw all successful", response));
    }
}
