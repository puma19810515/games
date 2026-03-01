package com.games.dto;

import com.games.enums.SportTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportTransactionMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long sportBetId;
    private Long userId;
    private Long merchantId;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private LocalDateTime timestamp;
}
