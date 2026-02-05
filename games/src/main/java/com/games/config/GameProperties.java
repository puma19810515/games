package com.games.config;

import lombok.Data;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

@Data
public class GameProperties {
    private List<String> symbols;
    private BigDecimal minBet;
    private BigDecimal maxBet;
    private BigDecimal initialBalance;
    private Double targetRtp;
    private LinkedHashMap<String, String> symbolDisplay;
    private LinkedHashMap<String, Double> symbolWeights;
    private LinkedHashMap<String, Double> payoutMultipliers;
    private BigDecimal twoMatchMultiplier;
    private boolean isImage;
}
