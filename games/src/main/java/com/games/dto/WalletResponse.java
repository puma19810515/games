package com.games.dto;

import com.games.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WalletResponse {
    private String username;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String message;
}
