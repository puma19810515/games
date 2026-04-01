package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.crawler.*;
import com.games.service.CrawlerDataService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 爬蟲資料接口
 *
 * 用於接收外部爬蟲服務推送的資料，包括：
 * - 聯賽 (leagues)
 * - 隊伍 (teams)
 * - 賽事 (sport_events)
 * - 盤口 (market_lines)
 *
 * 特點：
 * - 支援批量處理
 * - 自動處理重複資料（Upsert）
 * - 頻率限制防止過度呼叫
 * - 詳細的處理報告
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
@Tag(name = "爬蟲資料接口", description = "接收外部爬蟲服務推送的體育資料 - 聯賽、隊伍、賽事、盤口")
public class CrawlerController {

    private final CrawlerDataService crawlerDataService;

    // ==================== 聯賽 ====================

    @Operation(summary = "批量推送聯賽資料", description = """
            接收爬蟲推送的聯賽資料，支援批量處理。
            
            重複性處理：
            - 使用 externalLeagueId 作為唯一識別
            - 如果 externalLeagueId 已存在，則更新資料
            - 如果 externalLeagueId 不存在，則新增資料
            
            頻率限制：每分鐘最多 60 次
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "處理成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "請求參數錯誤"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "請求過於頻繁")
    })
    @PostMapping("/leagues")
    @RateLimiter(name = "crawlerLeagues")
    public ResponseEntity<ApiResponse<CrawlerResponse>> pushLeagues(
            @Valid @RequestBody List<CrawlerLeagueRequest> requests,
            @Parameter(description = "批次ID，用於追蹤") @RequestHeader(value = "X-Batch-Id", required = false) String batchId) {

        String actualBatchId = batchId != null ? batchId : generateBatchId();
        log.info("接收聯賽資料: batchId={}, count={}", actualBatchId, requests.size());

        CrawlerResponse response = crawlerDataService.processLeagues(requests, actualBatchId);
        return ResponseEntity.ok(ApiResponse.success("聯賽資料處理完成", response));
    }

    // ==================== 隊伍 ====================

    @Operation(summary = "批量推送隊伍資料", description = """
            接收爬蟲推送的隊伍資料，支援批量處理。
            
            重複性處理：
            - 使用 externalTeamId 作為唯一識別
            - 如果 externalTeamId 已存在，則更新資料
            - 如果 externalTeamId 不存在，則新增資料
            
            頻率限制：每分鐘最多 60 次
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "處理成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "請求參數錯誤"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "請求過於頻繁")
    })
    @PostMapping("/teams")
    @RateLimiter(name = "crawlerTeams")
    public ResponseEntity<ApiResponse<CrawlerResponse>> pushTeams(
            @Valid @RequestBody List<CrawlerTeamRequest> requests,
            @Parameter(description = "批次ID，用於追蹤") @RequestHeader(value = "X-Batch-Id", required = false) String batchId) {

        String actualBatchId = batchId != null ? batchId : generateBatchId();
        log.info("接收隊伍資料: batchId={}, count={}", actualBatchId, requests.size());

        CrawlerResponse response = crawlerDataService.processTeams(requests, actualBatchId);
        return ResponseEntity.ok(ApiResponse.success("隊伍資料處理完成", response));
    }

    // ==================== 賽事 ====================

    @Operation(summary = "批量推送賽事資料", description = """
            接收爬蟲推送的賽事資料，支援批量處理。
            
            重複性處理：
            - 使用 externalEventId 作為唯一識別
            - 如果 externalEventId 已存在，則更新資料
            - 如果 externalEventId 不存在，則新增資料
            
            依賴關係：
            - 需要先推送隊伍資料（externalHomeTeamId, externalAwayTeamId）
            - 聯賽可選（externalLeagueId）
            
            頻率限制：每分鐘最多 120 次
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "處理成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "請求參數錯誤"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "請求過於頻繁")
    })
    @PostMapping("/events")
    @RateLimiter(name = "crawlerEvents")
    public ResponseEntity<ApiResponse<CrawlerResponse>> pushEvents(
            @Valid @RequestBody List<CrawlerEventRequest> requests,
            @Parameter(description = "批次ID，用於追蹤") @RequestHeader(value = "X-Batch-Id", required = false) String batchId) {

        String actualBatchId = batchId != null ? batchId : generateBatchId();
        log.info("接收賽事資料: batchId={}, count={}", actualBatchId, requests.size());

        CrawlerResponse response = crawlerDataService.processEvents(requests, actualBatchId);
        return ResponseEntity.ok(ApiResponse.success("賽事資料處理完成", response));
    }

    // ==================== 盤口 ====================

    @Operation(summary = "批量推送盤口賠率資料", description = """
            接收爬蟲推送的盤口賠率資料，支援批量處理。
            
            重複性處理：
            - 使用 externalMarketId 作為唯一識別
            - 如果 externalMarketId 已存在，則更新資料
            - 如果 externalMarketId 不存在，則新增資料
            
            依賴關係：
            - 需要先推送賽事資料（externalEventId）
            - 需要預先建立玩法類型（betTypeCode）
            - 需要預先建立賠率格式（oddsFormatCode）
            
            頻率限制：每分鐘最多 300 次（盤口變化頻繁）
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "處理成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "請求參數錯誤"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "請求過於頻繁")
    })
    @PostMapping("/market-lines")
    @RateLimiter(name = "crawlerMarketLines")
    public ResponseEntity<ApiResponse<CrawlerResponse>> pushMarketLines(
            @Valid @RequestBody List<CrawlerMarketLineRequest> requests,
            @Parameter(description = "批次ID，用於追蹤") @RequestHeader(value = "X-Batch-Id", required = false) String batchId) {

        String actualBatchId = batchId != null ? batchId : generateBatchId();
        log.info("接收盤口資料: batchId={}, count={}", actualBatchId, requests.size());

        CrawlerResponse response = crawlerDataService.processMarketLines(requests, actualBatchId);
        return ResponseEntity.ok(ApiResponse.success("盤口資料處理完成", response));
    }

    // ==================== 輔助方法 ====================

    private String generateBatchId() {
        return "BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
