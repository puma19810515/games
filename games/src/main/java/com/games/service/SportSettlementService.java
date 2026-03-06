package com.games.service;

import com.games.entity.*;
import com.games.enums.*;
import com.games.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SportSettlementService {

    private final SportBetRepository sportBetRepository;
    private final BetLegRepository betLegRepository;
    private final SportEventRepository sportEventRepository;
    private final SportTransactionRepository sportTransactionRepository;
    private final UserRepository userRepository;
    private final OddsConversionService oddsConversionService;

    /**
     * 結算指定賽事的所有投注
     */
    @Transactional
    public int settleEventBets(Long eventId, Integer homeScore, Integer awayScore,
                               Integer homeScoreHalf, Integer awayScoreHalf) {
        // 1. 更新賽事比分
        SportEvent event = sportEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("賽事不存在: " + eventId));

        event.setHomeScore(homeScore);
        event.setAwayScore(awayScore);
        event.setHomeScoreHalf(homeScoreHalf);
        event.setAwayScoreHalf(awayScoreHalf);
        event.setSportEventStatus(SportEventStatus.FINISHED);
        event.setSportEventSettleStatus(SportEventSettleStatus.SETTLED);
        sportEventRepository.save(event);

        // 2. 找出所有待結算的投注腿
        List<BetLeg> pendingLegs = betLegRepository.findPendingByEventId(eventId);
        log.info("賽事 {} 有 {} 筆待結算投注腿", eventId, pendingLegs.size());

        // 3. 結算每一腿
        for (BetLeg leg : pendingLegs) {
            settleBetLeg(leg, event);
        }

        // 4. 檢查並結算完整投注單
        // Fix: 方法名稱修正為 findPendingByEventId
        List<SportBet> affectedBets = sportBetRepository.findPendingByEventId(eventId);
        for (SportBet bet : affectedBets) {
            checkAndSettleBet(bet);
        }

        return pendingLegs.size();
    }

    /**
     * 結算單一投注腿
     */
    private void settleBetLeg(BetLeg leg, SportEvent event) {
        String betTypeCode = leg.getBetTypeCode();
        SettlementResult result;

        // 根據玩法計算結果
        if (betTypeCode.startsWith("AH") || betTypeCode.contains("_AH")) {
            result = settleAsianHandicap(leg, event);
        } else if (betTypeCode.startsWith("OU") || betTypeCode.contains("_OU")) {
            result = settleOverUnder(leg, event);
        } else if (betTypeCode.equals("1X2") || betTypeCode.equals("1X2_HALF")) {
            result = settle1X2(leg, event);
        } else if (betTypeCode.equals("CS") || betTypeCode.equals("CS_HALF")) {
            result = settleCorrectScore(leg, event);
        } else if (betTypeCode.equals("OE")) {
            result = settleOddEven(leg, event);
        } else if (betTypeCode.equals("BTTS")) {
            result = settleBothTeamsToScore(leg, event);
        } else if (betTypeCode.startsWith("US_")) {
            result = settleAmericanLine(leg, event);
        } else {
            log.warn("未支援的玩法: {}", betTypeCode);
            result = SettlementResult.VOID;
        }

        // 更新結果和係數
        leg.setResult(result);
        leg.setResultFactor(getResultFactor(result));
        betLegRepository.save(leg);

        log.info("投注腿 {} 結算完成: {} (係數: {})", leg.getId(), result, leg.getResultFactor());
    }

    /**
     * 亞洲讓球盤結算
     */
    private SettlementResult settleAsianHandicap(BetLeg leg, SportEvent event) {
        BigDecimal handicap = leg.getHandicap();
        // Fix: 使用 BetLegSelection enum，而非 String
        BetLegSelection selection = leg.getSelection();
        boolean isHalfTime = leg.getBetTypeCode().contains("HALF");

        // Fix: 使用 Integer 避免 NPE
        Integer homeScore = isHalfTime ? event.getHomeScoreHalf() : event.getHomeScore();
        Integer awayScore = isHalfTime ? event.getAwayScoreHalf() : event.getAwayScore();

        if (homeScore == null || awayScore == null) {
            return SettlementResult.VOID;
        }

        // Fix: 使用 BetLegSelection.HOME 比較
        BigDecimal diff;
        if (BetLegSelection.HOME == selection) {
            diff = BigDecimal.valueOf(homeScore - awayScore).add(handicap);
        } else {
            diff = BigDecimal.valueOf(awayScore - homeScore).add(handicap.negate());
        }

        return calculateAsianResult(diff, handicap);
    }

    /**
     * 大小盤結算
     */
    private SettlementResult settleOverUnder(BetLeg leg, SportEvent event) {
        BigDecimal line = leg.getHandicap();
        // Fix: 使用 BetLegSelection enum
        BetLegSelection selection = leg.getSelection();
        boolean isHalfTime = leg.getBetTypeCode().contains("HALF");

        // Fix: 使用 Integer 避免 NPE
        Integer homeScore = isHalfTime ? event.getHomeScoreHalf() : event.getHomeScore();
        Integer awayScore = isHalfTime ? event.getAwayScoreHalf() : event.getAwayScore();

        if (homeScore == null || awayScore == null) {
            return SettlementResult.VOID;
        }

        int totalScore = homeScore + awayScore;
        BigDecimal diff;

        // Fix: 使用 BetLegSelection.OVER 比較
        if (BetLegSelection.OVER == selection) {
            diff = BigDecimal.valueOf(totalScore).subtract(line);
        } else {
            diff = line.subtract(BigDecimal.valueOf(totalScore));
        }

        return calculateAsianResult(diff, line);
    }

    /**
     * 獨贏盤(1X2)結算
     */
    private SettlementResult settle1X2(BetLeg leg, SportEvent event) {
        // Fix: 使用 BetLegSelection enum
        BetLegSelection selection = leg.getSelection();
        boolean isHalfTime = leg.getBetTypeCode().contains("HALF");

        // Fix: 使用 Integer 避免 NPE
        Integer homeScore = isHalfTime ? event.getHomeScoreHalf() : event.getHomeScore();
        Integer awayScore = isHalfTime ? event.getAwayScoreHalf() : event.getAwayScore();

        if (homeScore == null || awayScore == null) {
            return SettlementResult.VOID;
        }

        // Fix: 使用 BetLegSelection enum 比較，避免 String vs Enum 永遠 false
        BetLegSelection actualResult;
        if (homeScore > awayScore) {
            actualResult = BetLegSelection.HOME;
        } else if (awayScore > homeScore) {
            actualResult = BetLegSelection.AWAY;
        } else {
            actualResult = BetLegSelection.DRAW;
        }

        return selection == actualResult ? SettlementResult.WIN : SettlementResult.LOSE;
    }

    /**
     * 波膽結算
     * 注意：BetLeg.selection 為 BetLegSelection enum，無法儲存正確比分字串
     * 若需完整支援波膽，需在 BetLeg 新增 correctScore 欄位
     */
    private SettlementResult settleCorrectScore(BetLeg leg, SportEvent event) {
        // Fix: 使用 Integer 避免 NPE
        Integer homeScore = leg.getBetTypeCode().contains("HALF")
                ? event.getHomeScoreHalf() : event.getHomeScore();
        Integer awayScore = leg.getBetTypeCode().contains("HALF")
                ? event.getAwayScoreHalf() : event.getAwayScore();

        if (homeScore == null || awayScore == null) {
            return SettlementResult.VOID;
        }

        // BetLeg 目前沒有 correctScore 欄位，無法結算波膽，先回傳 VOID
        log.warn("投注腿 {} 為波膽玩法，BetLeg 目前不支援 correctScore，結算為 VOID", leg.getId());
        return SettlementResult.VOID;
    }

    /**
     * 單雙盤結算
     */
    private SettlementResult settleOddEven(BetLeg leg, SportEvent event) {
        // Fix: 使用 BetLegSelection enum
        BetLegSelection selection = leg.getSelection();

        Integer homeScore = event.getHomeScore();
        Integer awayScore = event.getAwayScore();

        if (homeScore == null || awayScore == null) {
            return SettlementResult.VOID;
        }

        int total = homeScore + awayScore;
        boolean isOdd = total % 2 != 0;

        // Fix: 使用 BetLegSelection.ODD 比較
        if (BetLegSelection.ODD == selection) {
            return isOdd ? SettlementResult.WIN : SettlementResult.LOSE;
        }
        return isOdd ? SettlementResult.LOSE : SettlementResult.WIN;
    }

    /**
     * 兩隊都進球結算
     */
    private SettlementResult settleBothTeamsToScore(BetLeg leg, SportEvent event) {
        // Fix: 使用 BetLegSelection enum
        BetLegSelection selection = leg.getSelection();

        Integer homeScore = event.getHomeScore();
        Integer awayScore = event.getAwayScore();

        if (homeScore == null || awayScore == null) {
            return SettlementResult.VOID;
        }

        boolean bothScored = homeScore > 0 && awayScore > 0;

        // Fix: 使用 BetLegSelection.YES 比較
        if (BetLegSelection.YES == selection) {
            return bothScored ? SettlementResult.WIN : SettlementResult.LOSE;
        }
        return bothScored ? SettlementResult.LOSE : SettlementResult.WIN;
    }

    /**
     * 美國盤結算
     */
    private SettlementResult settleAmericanLine(BetLeg leg, SportEvent event) {
        String betTypeCode = leg.getBetTypeCode();

        if ("US_ML".equals(betTypeCode)) {
            return settle1X2(leg, event);
        } else if ("US_SPREAD".equals(betTypeCode)) {
            return settleAsianHandicap(leg, event);
        } else if ("US_TOTAL".equals(betTypeCode)) {
            return settleOverUnder(leg, event);
        }

        return SettlementResult.VOID;
    }

    /**
     * 計算亞洲盤結果（支援半球、四分球）
     */
    private SettlementResult calculateAsianResult(BigDecimal diff, BigDecimal line) {
        // 四分盤判斷 (0.25, 0.75)
        BigDecimal fraction = line.abs().remainder(BigDecimal.ONE);
        boolean isQuarterLine = fraction.compareTo(new BigDecimal("0.25")) == 0
                || fraction.compareTo(new BigDecimal("0.75")) == 0;

        if (isQuarterLine) {
            // 四分盤：贏半/輸半
            if (diff.abs().compareTo(new BigDecimal("0.25")) <= 0) {
                return diff.compareTo(BigDecimal.ZERO) > 0
                        ? SettlementResult.HALF_WIN
                        : SettlementResult.HALF_LOSE;
            }
        }

        // 整數盤：可能走盤
        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            return SettlementResult.PUSH;
        }

        return diff.compareTo(BigDecimal.ZERO) > 0
                ? SettlementResult.WIN
                : SettlementResult.LOSE;
    }

    /**
     * 取得結算係數
     */
    private BigDecimal getResultFactor(SettlementResult result) {
        return switch (result) {
            case WIN -> BigDecimal.ONE;
            case HALF_WIN -> new BigDecimal("0.5");
            case PUSH, VOID -> BigDecimal.ZERO;
            case HALF_LOSE -> new BigDecimal("-0.5");
            case LOSE -> BigDecimal.ONE.negate();
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 檢查並結算完整投注單
     */
    private void checkAndSettleBet(SportBet bet) {
        // Fix: 方法名稱修正為 findByBetIdWithDetails
        List<BetLeg> legs = betLegRepository.findByBetIdWithDetails(bet.getId());

        // Fix: 使用 SettlementResult enum 比較，而非 String
        boolean allSettled = legs.stream()
                .noneMatch(leg -> SettlementResult.PENDING == leg.getResult());

        if (!allSettled) {
            return;
        }

        // 計算派彩
        BigDecimal payout = calculatePayout(bet, legs);

        bet.setWinAmount(payout);
        bet.setStatus(SportBetStatus.SETTLED);
        bet.setSettledAt(LocalDateTime.now());
        sportBetRepository.save(bet);

        // 派彩到用戶錢包
        if (payout.compareTo(BigDecimal.ZERO) > 0) {
            creditUserWallet(bet, payout);
        }

        log.info("投注單 {} 結算完成，派彩: {}", bet.getId(), payout);
    }

    /**
     * 計算派彩金額
     */
    private BigDecimal calculatePayout(SportBet bet, List<BetLeg> legs) {
        BigDecimal stake = bet.getStake();

        // Fix: 使用 SportBetType enum 比較，而非 String
        if (SportBetType.SINGLE == bet.getBetType()) {
            // 單注
            BetLeg leg = legs.get(0);
            return calculateLegPayout(stake, leg);
        } else {
            // 串關：計算有效賠率
            BigDecimal effectiveOdds = BigDecimal.ONE;

            for (BetLeg leg : legs) {
                // Fix: leg.getResult() 已是 SettlementResult，直接使用
                SettlementResult result = leg.getResult();

                if (result == SettlementResult.LOSE) {
                    return BigDecimal.ZERO; // 任一腿輸，全輸
                }

                if (result == SettlementResult.VOID || result == SettlementResult.PUSH) {
                    continue; // 走盤的腿不計入賠率
                }

                BigDecimal legOdds = leg.getOddsDecimal();
                if (result == SettlementResult.HALF_WIN) {
                    legOdds = BigDecimal.ONE.add(
                            legOdds.subtract(BigDecimal.ONE).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP)
                    );
                } else if (result == SettlementResult.HALF_LOSE) {
                    return stake.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                }

                effectiveOdds = effectiveOdds.multiply(legOdds);
            }

            return stake.multiply(effectiveOdds).setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 計算單腿派彩（考慮盤口格式）
     */
    private BigDecimal calculateLegPayout(BigDecimal stake, BetLeg leg) {
        SettlementResult result = leg.getResult();
        String oddsFormat = leg.getOddsFormatCode();
        BigDecimal odds = leg.getOdds();

        // 馬來盤和印尼盤有特殊計算
        if ("MALAY".equals(oddsFormat)) {
            return oddsConversionService.calculateMalayPayout(stake, odds, result);
        }
        if ("INDO".equals(oddsFormat)) {
            return oddsConversionService.calculateIndoPayout(stake, odds, result);
        }

        // 其他盤口使用歐洲盤賠率計算
        BigDecimal decimalOdds = leg.getOddsDecimal();

        return switch (result) {
            case WIN -> stake.multiply(decimalOdds).setScale(2, RoundingMode.HALF_UP);
            case HALF_WIN -> {
                BigDecimal profit = stake.multiply(decimalOdds.subtract(BigDecimal.ONE))
                        .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                yield stake.add(profit);
            }
            case PUSH, VOID -> stake;
            case HALF_LOSE -> stake.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            case LOSE -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 派彩到用戶錢包
     */
    private void creditUserWallet(SportBet bet, BigDecimal amount) {
        User user = bet.getUser();
        BigDecimal balanceBefore = user.getSportBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        user.setSportBalance(balanceAfter);
        userRepository.save(user);

        // 記錄交易流水
        SportTransaction transaction = new SportTransaction();
        transaction.setMerchant(bet.getMerchant());
        transaction.setUser(user);
        transaction.setSportBet(bet);
        transaction.setType(SportTransactionType.SPORT_WIN);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription("體育派彩 - 投注單#" + bet.getId());
        sportTransactionRepository.save(transaction);
    }
}
