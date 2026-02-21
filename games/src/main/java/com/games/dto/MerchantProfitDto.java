package com.games.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MerchantProfitDto {
    private BigDecimal totalBetAmount;
    private BigDecimal totalWinAmount;
}
