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
        try {
            List<SportEventResponse> events = sportEventService.getOpenEvents(sportTypeCode);
            return ResponseEntity.ok(ApiResponse.success(events));
        } catch (Exception e) {
            log.error("查詢賽事列表失敗", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 查詢賽事詳情（含盤口）
     *
     * @param eventId 賽事ID
     * @return 賽事詳情
     */
    @GetMapping("/detail/{eventId}")
    public ResponseEntity<ApiResponse<SportEventResponse>> getEventDetail(
            @PathVariable Long eventId) {
        try {
            SportEventResponse event = sportEventService.getEventDetail(eventId);
            return ResponseEntity.ok(ApiResponse.success(event));
        } catch (Exception e) {
            log.error("查詢賽事詳情失敗", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 分頁查詢賽事
     *
     * @param sportTypeId 球種ID（可選）
     * @param leagueId 聯賽ID（可選）
     * @param page 頁碼
     * @param size 每頁大小
     * @return 賽事分頁資料
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<SportEventResponse>>> getEventsPage(
            @RequestParam(required = false) Long sportTypeId,
            @RequestParam(required = false) Long leagueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<SportEventResponse> events = sportEventService.getEventsByFilters(
                    sportTypeId, leagueId, page, size);
            return ResponseEntity.ok(ApiResponse.success(events));
        } catch (Exception e) {
            log.error("分頁查詢賽事失敗", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
