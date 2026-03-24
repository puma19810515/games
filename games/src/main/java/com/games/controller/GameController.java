package com.games.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.games.dto.*;
import com.games.entity.Merchant;
import com.games.entity.User;
import com.games.service.AuthService;
import com.games.service.SlotGameService;
import com.games.util.PageDataResUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
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
public class GameController {

    private final SlotGameService slotGameService;
    private final AuthService authService;

    @PostMapping("/spin/{gameCode}")
    @RateLimiter(name = "spin")
    public ResponseEntity<ApiResponse<BetResponse>> spin(@PathVariable String gameCode, @Valid @RequestBody BetRequest request,
                                                         Authentication authentication,
                                                         @RequestAttribute(name = "merchant", required = false) Merchant merchant)
            throws JsonProcessingException {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        BetResponse response = slotGameService.placeBet(merchant, user, gameCode, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBalance(Authentication authentication,
                                                                       @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);

        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("gameBalance", user.getGameBalance());
        data.put("sportBalance", user.getSportBalance());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/records")
    public ResponseEntity<ApiResponse<PageDataResUtil<BetRecordsResponse>>> getRecords(Authentication authentication,
                                                                                       @RequestBody BetRecordsRequest request,
                                                                                       @RequestAttribute(name = "merchant", required = false) Merchant merchant) {
        String username = authentication.getName();
        User user = authService.getUserByUsername(merchant.getId(), username);
        PageDataResUtil<BetRecordsResponse> data = slotGameService.getBetRecords(user, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
