package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.AuthResponse;
import com.games.dto.LoginRequest;
import com.games.dto.RegisterRequest;
import com.games.entity.Merchant;
import com.games.service.AuthService;
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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "認證管理", description = "用戶註冊、登入、登出相關 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用戶註冊", description = "新用戶註冊，需提供商戶 API Key")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "註冊成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "請求參數錯誤"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "商戶驗證失敗")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest regRequest,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        AuthResponse response = authService.register(regRequest, merchant);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @Operation(summary = "用戶登入", description = "用戶登入，返回 JWT Token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登入成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "請求參數錯誤"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "帳號或密碼錯誤")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        AuthResponse response = authService.login(loginRequest, merchant);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @Operation(summary = "用戶登出", description = "登出當前用戶")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登出成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授權")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        authService.logout(merchant.getUsername(), username);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
