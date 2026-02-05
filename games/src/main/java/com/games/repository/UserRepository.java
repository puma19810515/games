package com.games.repository;

import com.games.annotation.ReadOnly;
import com.games.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户 - 使用从库
     */
    @ReadOnly
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在 - 使用从库
     */
    @ReadOnly
    boolean existsByUsername(String username);

    /**
     * 使用悲观锁查询用户（SELECT FOR UPDATE）- 必须使用主库
     * 在事务结束前，其他事务无法读取或修改此记录
     * 用于高并发场景下的余额操作，防止超扣
     *
     * 注意：此方法不添加@ReadOnly，因为需要在主库执行悲观锁
     *
     * @param id 用户ID
     * @return 用户实体（带锁）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithLock(@Param("id") Long id);
}
