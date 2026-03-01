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
    @RateLimiter(name = "sportDeposit", fallbackMethod = "sportDepositFallback")
    public ResponseEntity<ApiResponse<SportWalletResponse>> sportDeposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication,
            @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        try {
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 存款限流降级方法
    public ResponseEntity<ApiResponse<SportWalletResponse>> sportDepositFallback(DepositRequest request, Authentication authentication,
                                                                            Merchant merchant, Throwable t) {
        return ResponseEntity.status(429)
                .body(ApiResponse.error("Too many sport deposit requests. Please try again later."));
    }

    @PostMapping("/withdraw-all")
    @RateLimiter(name = "sportWithdraw", fallbackMethod = "sportWithdrawAllFallback")
    public ResponseEntity<ApiResponse<SportWalletResponse>> withdrawAll(Authentication authentication,
                                                                   @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        try {
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 提款限流降级方法
    public ResponseEntity<ApiResponse<SportWalletResponse>> sportWithdrawAllFallback(Authentication authentication,
                                                                                Merchant merchant, Throwable t) {
        return ResponseEntity.status(429)
                .body(ApiResponse.error("Too many sport withdraw requests. Please try again later."));
    }
}
