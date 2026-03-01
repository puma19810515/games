package com.games.repository;

import com.games.entity.SportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SportTypeRepository extends JpaRepository<SportType, Long> {
}
