package com.games.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RtpUpdateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal betAmount;
    private BigDecimal winAmount;
    private String gameCode;
}
