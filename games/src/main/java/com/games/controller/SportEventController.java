package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.SportEventResponse;
import com.games.service.SportEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 體育賽事控制器
 *
 * 提供賽事相關接口：
 * - 查詢可投注賽事列表
 * - 查詢賽事詳情（含盤口）
 */
@Slf4j
@RestController
@RequestMapping("/api/sport/event")
@RequiredArgsConstructor
public class SportEventController {

    private final SportEventService sportEventService;

    /**
     * 查詢可投注的賽事列表
     *
     * @param sportTypeCode 球種代碼（可選），如 FOOTBALL, BASKETBALL
     * @return 賽事列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SportEventResponse>>> getOpenEvents(
            @RequestParam(required = false) String sportTypeCode) {
        List<SportEventResponse> events = sportEventService.getOpenEvents(sportTypeCode);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * 查詢賽事詳情（含盤口）
     */
    @GetMapping("/detail/{eventId}")
    public ResponseEntity<ApiResponse<SportEventResponse>> getEventDetail(
            @PathVariable Long eventId) {
        SportEventResponse event = sportEventService.getEventDetail(eventId);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    /**
     * 分頁查詢賽事
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<SportEventResponse>>> getEventsPage(
            @RequestParam(required = false) Long sportTypeId,
            @RequestParam(required = false) Long leagueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SportEventResponse> events = sportEventService.getEventsByFilters(sportTypeId, leagueId, page, size);
        return ResponseEntity.ok(ApiResponse.success(events));
    }
}
