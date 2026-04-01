package com.games.repository;

import com.games.entity.SportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SportTypeRepository extends JpaRepository<SportType, Long> {

    /**
     * 根據球種代碼查詢
     */
    Optional<SportType> findByCode(String code);
}
