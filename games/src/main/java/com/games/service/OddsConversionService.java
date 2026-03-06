package com.games.service;

import com.games.enums.SettlementResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class OddsConversionService {

    /**
     * 將任意盤口轉換為歐洲盤（十進制）
     */
    public BigDecimal toDecimalOdds(BigDecimal odds, String format) {
        return switch (format) {
            case "EUROPEAN" -> odds;
            case "HONGKONG", "INDIAN" -> odds.add(BigDecimal.ONE);
            case "MALAY" -> convertMalayToDecimal(odds);
            case "INDO" -> convertIndoToDecimal(odds);
            case "AMERICAN" -> convertAmericanToDecimal(odds);
            case "ASIAN" -> odds.add(BigDecimal.ONE); // 亞洲盤水錢格式
            default -> odds;
        };
    }

    private BigDecimal convertMalayToDecimal(BigDecimal odds) {
        if (odds.compareTo(BigDecimal.ZERO) >= 0) {
            return odds.add(BigDecimal.ONE);
        }
        return BigDecimal.ONE.divide(odds.abs(), 4, RoundingMode.HALF_UP).add(BigDecimal.ONE);
    }

    private BigDecimal convertIndoToDecimal(BigDecimal odds) {
        if (odds.compareTo(BigDecimal.ZERO) >= 0) {
            return odds.add(BigDecimal.ONE);
        }
        return BigDecimal.ONE.divide(odds.abs(), 4, RoundingMode.HALF_UP).add(BigDecimal.ONE);
    }

    private BigDecimal convertAmericanToDecimal(BigDecimal odds) {
        BigDecimal hundred = new BigDecimal("100");
        if (odds.compareTo(BigDecimal.ZERO) >= 0) {
            return odds.divide(hundred, 4, RoundingMode.HALF_UP).add(BigDecimal.ONE);
        }
        return hundred.divide(odds.abs(), 4, RoundingMode.HALF_UP).add(BigDecimal.ONE);
    }

    /**
     * 馬來盤派彩計算
     */
    public BigDecimal calculateMalayPayout(BigDecimal stake, BigDecimal odds, SettlementResult result) {
        if (result == SettlementResult.PUSH || result == SettlementResult.VOID) {
            return stake;
        }

        if (odds.compareTo(BigDecimal.ZERO) >= 0) {
            // 正水：贏得 stake * odds，輸則輸全部
            if (result == SettlementResult.WIN) {
                return stake.add(stake.multiply(odds));
            } else if (result == SettlementResult.HALF_WIN) {
                return stake.add(stake.multiply(odds).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
            } else if (result == SettlementResult.HALF_LOSE) {
                return stake.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            }
            return BigDecimal.ZERO;
        } else {
            // 負水：贏得 stake / |odds|，輸則輸 stake * |odds|
            if (result == SettlementResult.WIN) {
                return stake.add(stake.divide(odds.abs(), 2, RoundingMode.HALF_UP));
            } else if (result == SettlementResult.HALF_WIN) {
                return stake.add(stake.divide(odds.abs(), 2, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
            } else if (result == SettlementResult.HALF_LOSE) {
                return stake.subtract(stake.multiply(odds.abs())
                        .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
            }
            return BigDecimal.ZERO;
        }
    }

    /**
     * 印尼盤派彩計算
     */
    public BigDecimal calculateIndoPayout(BigDecimal stake, BigDecimal odds, SettlementResult result) {
        if (result == SettlementResult.PUSH || result == SettlementResult.VOID) {
            return stake;
        }

        if (odds.compareTo(BigDecimal.ZERO) >= 0) {
            if (result == SettlementResult.WIN) {
                return stake.add(stake.multiply(odds));
            } else if (result == SettlementResult.HALF_WIN) {
                return stake.add(stake.multiply(odds).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
            } else if (result == SettlementResult.HALF_LOSE) {
                return stake.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            }
            return BigDecimal.ZERO;
        } else {
            if (result == SettlementResult.WIN) {
                return stake.add(stake.divide(odds.abs(), 2, RoundingMode.HALF_UP));
            } else if (result == SettlementResult.HALF_WIN) {
                return stake.add(stake.divide(odds.abs(), 2, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
            } else if (result == SettlementResult.HALF_LOSE) {
                return stake.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            }
            return BigDecimal.ZERO;
        }
    }
}
