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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamesWallet")
@RequiredArgsConstructor
public class GamesWalletController {

    private final GamesWalletService gamesWalletService;
    private final AuthService authService;

    @PostMapping("/deposit")
    @RateLimiter(name = "gamesDeposit")
    public ResponseEntity<ApiResponse<WalletResponse>> gamesDeposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
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

    @PostMapping("/withdraw-all")
    @RateLimiter(name = "gamesWithdraw")
    public ResponseEntity<ApiResponse<WalletResponse>> withdrawAll(Authentication authentication,
                                                                   @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
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
