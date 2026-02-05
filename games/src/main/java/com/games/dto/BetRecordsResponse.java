package com.games.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.games.serializer.LocalDateTimeEpochMillisSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BetRecordsResponse {
    private Long id;
    private String gameCode;
    private BigDecimal betAmount;
    private BigDecimal winAmount;
    private Boolean isWin;
    private List<String> result;
    @JsonSerialize(using = LocalDateTimeEpochMillisSerializer.class)
    private LocalDateTime createdAt;
}
