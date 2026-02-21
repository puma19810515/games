package com.games.entity;

import com.games.enums.MerchantSettlementFlag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "merchants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_username", columnNames = "username")
        },
        indexes = {
                @Index(name = "idx_merchants_settlement_flag", columnList = "settlement_flag"),
                @Index(name = "idx_merchants_username", columnList = "username"),
                @Index(name = "idx_merchants_created_at", columnList = "created_at")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    @Id
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String remark;

    @Column(name = "api_key", nullable = false, length = 64)
    private String apiKey;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_flag", nullable = false, length = 8)
    private MerchantSettlementFlag settlementFlag;

    @Column(name = "settlement_ratio", nullable = false, precision = 5, scale = 2)
    private BigDecimal settlementRatio = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
