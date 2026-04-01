package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.SportTypeResponse;
import com.games.service.SportTypeService;
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
@RequestMapping("/api/sport-type")
@Tag(name = "球種管理", description = "體育球種相關 API - 足球、籃球、網球等")
public class SportTypeController {

    public final SportTypeService sportTypeService;

    @Operation(summary = "查詢所有球種", description = "查詢系統支援的所有體育球種")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功")
    })
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SportTypeResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(sportTypeService.findAll()));
    }

    @Operation(summary = "更新球種狀態", description = "啟用或停用指定的球種")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "球種不存在")
    })
    @PostMapping("/updateStatus/{id}/{status}")
    public ResponseEntity<ApiResponse<List<SportTypeResponse>>> oddsUpdateStatus(
            @Parameter(description = "球種ID") @PathVariable String id,
            @Parameter(description = "狀態：0-停用, 1-啟用") @PathVariable Integer status) {
        return ResponseEntity.ok(ApiResponse.success(sportTypeService.updateStatus(id, status)));
    }
}
