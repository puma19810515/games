package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.DepositRequest;
import com.games.dto.WalletResponse;
import com.games.entity.User;
import com.games.enums.TransactionType;
import com.games.service.AuthService;
import com.games.service.WalletService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final AuthService authService;

    @PostMapping("/deposit")
    @RateLimiter(name = "deposit", fallbackMethod = "depositFallback")
    public ResponseEntity<ApiResponse<WalletResponse>> deposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = authService.getUserByUsername(username);

            User updatedUser = walletService.deposit(user, request.getAmount());

            WalletResponse response = new WalletResponse(
                    updatedUser.getUsername(),
                    updatedUser.getBalance().subtract(request.getAmount()),
                    updatedUser.getBalance(),
                    request.getAmount(),
                    TransactionType.DEPOSIT,
                    "Deposit successful"
            );

            return ResponseEntity.ok(ApiResponse.success("Deposit successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 存款限流降级方法
    public ResponseEntity<ApiResponse<WalletResponse>> depositFallback(DepositRequest request, Authentication authentication, Throwable t) {
        return ResponseEntity.status(429)
                .body(ApiResponse.error("Too many deposit requests. Please try again later."));
    }

    @PostMapping("/withdraw-all")
    @RateLimiter(name = "withdraw", fallbackMethod = "withdrawAllFallback")
    public ResponseEntity<ApiResponse<WalletResponse>> withdrawAll(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = authService.getUserByUsername(username);

            User updatedUser = walletService.withdrawAll(user);

            WalletResponse response = new WalletResponse(
                    updatedUser.getUsername(),
                    user.getBalance(),
                    updatedUser.getBalance(),
                    user.getBalance(),
                    TransactionType.WITHDRAW,
                    "Withdraw all successful"
            );

            return ResponseEntity.ok(ApiResponse.success("Withdraw all successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 提款限流降级方法
    public ResponseEntity<ApiResponse<WalletResponse>> withdrawAllFallback(Authentication authentication, Throwable t) {
        return ResponseEntity.status(429)
                .body(ApiResponse.error("Too many withdraw requests. Please try again later."));
    }
}
