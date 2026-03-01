package com.games.dto;

import com.games.enums.SportTransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SportWalletResponse {
    private String username;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private BigDecimal amount;
    private SportTransactionType sportTransactionType;
    private String message;
}
