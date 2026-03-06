package com.games.dto;

import lombok.Data;

@Data
public class SettleEventRequest {
    private Integer homeScore;
    private Integer awayScore;
    private Integer homeScoreHalf;
    private Integer awayScoreHalf;
}