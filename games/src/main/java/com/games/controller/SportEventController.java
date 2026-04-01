package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.SportEventResponse;
import com.games.service.SportEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 體育賽事控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/sport/event")
@RequiredArgsConstructor
@Tag(name = "體育賽事", description = "體育賽事相關 API - 查詢可投注賽事、賽事詳情等功能")
public class SportEventController {

    private final SportEventService sportEventService;

    @Operation(summary = "查詢可投注賽事列表", description = "查詢當前開放投注的賽事列表，可按球種篩選")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功")
    })
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SportEventResponse>>> getOpenEvents(
            @Parameter(description = "球種代碼，如 FOOTBALL, BASKETBALL") @RequestParam(required = false) String sportTypeCode) {
        List<SportEventResponse> events = sportEventService.getOpenEvents(sportTypeCode);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @Operation(summary = "查詢賽事詳情", description = "根據賽事ID查詢詳細資訊，包含所有可用盤口")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "賽事不存在")
    })
    @GetMapping("/detail/{eventId}")
    public ResponseEntity<ApiResponse<SportEventResponse>> getEventDetail(
            @Parameter(description = "賽事ID") @PathVariable Long eventId) {
        SportEventResponse event = sportEventService.getEventDetail(eventId);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @Operation(summary = "分頁查詢賽事", description = "分頁查詢賽事列表，支援按球種和聯賽篩選")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功")
    })
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<SportEventResponse>>> getEventsPage(
            @Parameter(description = "球種ID") @RequestParam(required = false) Long sportTypeId,
            @Parameter(description = "聯賽ID") @RequestParam(required = false) Long leagueId,
            @Parameter(description = "頁碼，從0開始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "10") int size) {
        Page<SportEventResponse> events = sportEventService.getEventsByFilters(sportTypeId, leagueId, page, size);
        return ResponseEntity.ok(ApiResponse.success(events));
    }
}
