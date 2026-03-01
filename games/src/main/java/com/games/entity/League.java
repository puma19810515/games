package com.games.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leagues",
        indexes = {
                @Index(name = "idx_leagues_sport_type", columnList = "sport_type_id"),
                @Index(name = "idx_leagues_external_id", columnList = "external_league_id"),
                @Index(name = "idx_leagues_status", columnList = "status")
        })
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK → sport_types.id
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_type_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_leagues_sport_type"))
    private SportType sportType;

    /**
     * External ID from crawler or odds provider
     */
    @Column(name = "external_league_id", length = 50)
    private String externalLeagueId;

    /**
     * League name
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Country / region
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code")
    private Country country;

    /**
     * display order
     */
    @Column(name = "display_order", nullable = false, length = 2)
    private Integer displayOrder = 0;

    /**
     * 0 = disabled
     * 1 = enabled
     */
    @Column(name = "status", nullable = false, length = 1)
    private Integer status = 1;

    /**
     * PostgreSQL TIMESTAMP WITH TIME ZONE
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
