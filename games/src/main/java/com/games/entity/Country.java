package com.games.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "countries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    @Id
    @Column(length = 2)
    private String code;

    @Column(name = "code_alpha3", nullable = false, unique = true, length = 3)
    private String codeAlpha3;

    @Column(name = "code_numeric", nullable = false, unique = true, length = 3)
    private String codeNumeric;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_zh", nullable = false, length = 100)
    private String nameZh;

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
