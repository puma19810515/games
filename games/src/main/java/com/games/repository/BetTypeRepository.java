package com.games.repository;

import com.games.entity.BetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BetTypeRepository extends JpaRepository<BetType, Long> {
}
