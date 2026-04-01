package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.LeagueResponse;
import com.games.service.LeagueService;
import com.games.util.PageDataResUtil;
import com.games.util.PageReqUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/league")
@RequiredArgsConstructor
@Tag(name = "聯賽管理", description = "聯賽相關 API")
public class LeagueController {

    private final LeagueService leagueService;

    @Operation(summary = "分頁查詢聯賽列表", description = "分頁查詢所有可用的聯賽")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功")
    })
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageDataResUtil<LeagueResponse>>> getListByPage(
            @RequestBody PageReqUtil request) {
        return ResponseEntity.ok(ApiResponse.success(leagueService.getListByPage(request)));
    }
}
