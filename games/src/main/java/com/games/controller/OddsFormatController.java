package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.OddsFormatResponse;
import com.games.service.OddsFormatService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/odds-format")
@Tag(name = "賠率格式", description = "賠率格式相關 API - 亞洲盤、歐洲盤、香港盤、馬來盤、印尼盤、美國盤、印度盤等")
public class OddsFormatController {

    public final OddsFormatService oddsFormatService;

    @Operation(summary = "查詢所有賠率格式", description = "查詢系統支援的所有賠率格式")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功")
    })
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<OddsFormatResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(oddsFormatService.findAll()));
    }

    @Operation(summary = "更新賠率格式狀態", description = "啟用或停用指定的賠率格式")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "賠率格式不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "請求過於頻繁")
    })
    @PostMapping("/updateStatus/{id}/{status}")
    @RateLimiter(name = "oddsFormatUpdateStatus")
    public ResponseEntity<ApiResponse<List<OddsFormatResponse>>> oddsUpdateStatus(
            @Parameter(description = "賠率格式ID") @PathVariable String id,
            @Parameter(description = "狀態：0-停用, 1-啟用") @PathVariable Integer status) {
        return ResponseEntity.ok(ApiResponse.success(oddsFormatService.updateStatus(id, status)));
    }
}
