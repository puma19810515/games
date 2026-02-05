package com.games.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class BetResponse {
    private Long betId;
    private List<String> result;
    private BigDecimal betAmount;
    private BigDecimal winAmount;
    private Boolean isWin;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String message;
}