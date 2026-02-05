package com.games.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private BigDecimal balance;
}
