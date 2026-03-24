package com.games.controller;

import com.games.dto.ApiResponse;
import com.games.dto.SportTypeResponse;
import com.games.service.SportTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sport-type")
public class SportTypeController {

    public final SportTypeService sportTypeService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SportTypeResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(sportTypeService.findAll()));
    }

    @PostMapping("/updateStatus/{id}/{status}")
    public ResponseEntity<ApiResponse<List<SportTypeResponse>>> oddsUpdateStatus(
            @PathVariable String id, @PathVariable Integer status) {
        return ResponseEntity.ok(ApiResponse.success(sportTypeService.updateStatus(id, status)));
    }
}
