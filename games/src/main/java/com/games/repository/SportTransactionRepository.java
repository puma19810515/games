package com.games.repository;

import com.games.entity.SportTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SportTransactionRepository extends JpaRepository<SportTransaction, Long> {
}
