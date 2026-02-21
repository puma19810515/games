package com.games.scheduler;

import com.games.dto.MerchantProfitDto;
import com.games.entity.Merchant;
import com.games.enums.MerchantSettlementFlag;
import com.games.repository.BetRepository;
import com.games.repository.MerchantProfitReportRepository;
import com.games.repository.MerchantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class MerchantProfitScheduler {

    private final BetRepository betRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantProfitReportRepository merchantProfitReportRepository;

    public MerchantProfitScheduler(BetRepository betRepository, MerchantRepository merchantRepository,
                                   MerchantProfitReportRepository merchantProfitReportRepository) {
        this.betRepository = betRepository;
        this.merchantRepository = merchantRepository;
        this.merchantProfitReportRepository = merchantProfitReportRepository;
    }

    @Scheduled(cron = "0 5 0 * * ?", zone = "Asia/Taipei")
    @Transactional
    public void runMerchantProfitReport() {
        log.info("-- runMerchantProfitReport start --");
        LocalDate yesterday = LocalDate.now().plusDays(-1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.plusDays(1).atStartOfDay();
        log.info("-- runMerchantProfitReport startTime: {}, endTime: {} --", startOfDay, endOfDay);
        // 1. 获取所有商户
        List<Merchant> merchants = merchantRepository.findAllByEnableStatus();
        for (Merchant merchant : merchants) {
            // 2. 计算商户利润（示例逻辑，实际应根据业务需求计算）
            BigDecimal profit = calculateMerchantProfit(merchant, startOfDay, endOfDay);
            // 3. 保存利润报告
            saveMerchantProfitReport(merchant, profit);
        }
        log.info("-- runMerchantProfitReport end --");
    }

    private BigDecimal calculateMerchantProfit(Merchant merchant, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        log.info("-- calculateMerchantProfit merchantId:{}, startTime:{}, endTime:{} --", merchant.getId(), startOfDay, endOfDay);
        MerchantProfitDto profitDto = betRepository.findMerchantProfit(merchant.getId(), startOfDay, endOfDay);
        BigDecimal profit = profitDto.getTotalBetAmount().subtract(profitDto.getTotalWinAmount());
        log.info("-- calculateMerchantProfit merchantId:{}, profit:{} --", merchant.getId(), profit);
        return profit;
    }

    private void saveMerchantProfitReport(Merchant merchant, BigDecimal profit) {
    }
}
