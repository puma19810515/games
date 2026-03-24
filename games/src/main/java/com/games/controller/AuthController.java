package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.AuthResponse;
import com.games.dto.LoginRequest;
import com.games.dto.RegisterRequest;
import com.games.entity.Merchant;
import com.games.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest regRequest,
                                                              @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        AuthResponse response = authService.register(regRequest, merchant);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                           @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        AuthResponse response = authService.login(loginRequest, merchant);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(Authentication authentication,
                                                      @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        authService.logout(merchant.getUsername(), username);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
