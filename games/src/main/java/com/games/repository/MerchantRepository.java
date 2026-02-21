package com.games.repository;

import com.games.annotation.ReadOnly;
import com.games.entity.Merchant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    @ReadOnly
    @Query("SELECT m FROM Merchant m WHERE m.apiKey = :apiKey")
    Merchant findByApiKey(@Param("apiKey") String apiKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Merchant m WHERE m.status = 1")
    List<Merchant> findAllByEnableStatus();
}
