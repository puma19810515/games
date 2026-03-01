package com.games.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 賠率格式表
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "odds_formats", indexes = {
        @Index(name = "idx_odds_formats_code", columnList = "code")
})
public class OddsFormat {

    /**
     * 賠率格式ID，主鍵
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 賠率格式代碼：ASIAN-亞洲盤, EUROPEAN-歐洲盤, HONGKONG-香港盤,
     * MALAY-馬來盤, INDO-印尼盤, AMERICAN-美國盤, INDIAN-印度盤
     */
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    /**
     * 中文名稱
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 英文名稱
     */
    @Column(name = "name_en", nullable = false, length = 50)
    private String nameEn;

    /**
     * 格式說明
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 狀態：0-停用, 1-啟用
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Integer status = 1;

    /**
     * 建立時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
