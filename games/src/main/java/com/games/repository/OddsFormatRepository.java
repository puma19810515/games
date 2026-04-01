package com.games.repository;

import com.games.entity.OddsFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OddsFormatRepository extends JpaRepository<OddsFormat, Long> {

    /**
     * 根據賠率格式代碼查詢
     */
    Optional<OddsFormat> findByCode(String code);
}
