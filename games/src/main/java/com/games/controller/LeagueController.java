package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.LeagueResponse;
import com.games.service.LeagueService;
import com.games.util.PageDataResUtil;
import com.games.util.PageReqUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/league")
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageDataResUtil<LeagueResponse>>> getListByPage(
            @RequestBody PageReqUtil request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(leagueService.getListByPage(request)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
