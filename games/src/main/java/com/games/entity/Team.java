package com.games.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "teams",
        indexes = {
                @Index(name = "idx_teams_sport_type", columnList = "sport_type_id"),
                @Index(name = "idx_teams_external_id", columnList = "external_team_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK to sport_types.id
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sport_type_id", nullable = false,
            foreignKey = @ForeignKey(name = "teams_sport_type_id_fkey"))
    private SportType sportType;

    /**
     * External crawler team id
     */
    @Column(name = "external_team_id", length = 50)
    private String externalTeamId;

    /**
     * Full team name
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Short name (e.g. MUN)
     */
    @Column(name = "short_name", length = 20)
    private String shortName;

    /**
     * Logo URL
     */
    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    /**
     * Created time
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Updated time
     */
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