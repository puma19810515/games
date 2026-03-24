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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sportWallet")
@RequiredArgsConstructor
public class SportWalletController {

    private final SportWalletService sportsWalletService;
    private final AuthService authService;

    @PostMapping("/deposit")
    @RateLimiter(name = "sportDeposit")
    public ResponseEntity<ApiResponse<SportWalletResponse>> sportDeposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
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

    @PostMapping("/withdraw-all")
    @RateLimiter(name = "sportWithdraw")
    public ResponseEntity<ApiResponse<SportWalletResponse>> withdrawAll(Authentication authentication,
                                                                        @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
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
