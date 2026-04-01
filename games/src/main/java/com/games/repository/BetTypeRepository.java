package com.games.repository;

import com.games.entity.BetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BetTypeRepository extends JpaRepository<BetType, Long> {

    /**
     * 根據玩法代碼查詢
     */
    Optional<BetType> findByCode(String code);
}
