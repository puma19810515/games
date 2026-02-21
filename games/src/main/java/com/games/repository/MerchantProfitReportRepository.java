package com.games.repository;

import com.games.entity.MerchantProfitReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantProfitReportRepository extends JpaRepository<MerchantProfitReport, Long> {
}
