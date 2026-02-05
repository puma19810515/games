package com.games.repository;

import com.games.annotation.ReadOnly;
import com.games.entity.Transaction;
import com.games.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 查询用户的交易记录（按创建时间降序）- 使用从库
     */
    @ReadOnly
    List<Transaction> findByUserOrderByCreatedAtDesc(User user);
}
