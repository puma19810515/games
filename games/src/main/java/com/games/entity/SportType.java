package com.games.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 運動種類表（SportType）
 *
 * 例如：
 * - FOOTBALL（足球）
 * - CRICKET（板球）
 *
 * 用於分類賽事，管理顯示順序與啟用狀態
 */
@Entity
@Table(
        name = "sport_types",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sport_types_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_sport_types_display_order", columnList = "display_order"),
                @Index(name = "idx_sport_types_status", columnList = "status")
        }
)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SportType {

    /** 運動種類ID，主鍵 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 運動代碼（唯一），例如 FOOTBALL、CRICKET */
    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    /** 運動名稱，用於前端顯示，例如「足球」「板球」 */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** 顯示順序，數字越小排越前 */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /** 狀態：0 = disabled, 1 = enabled */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /** 建立時間（PostgreSQL TIMESTAMP WITH TIME ZONE） */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 新增時自動填入時間 */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** 更新時自動刷新時間 */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}