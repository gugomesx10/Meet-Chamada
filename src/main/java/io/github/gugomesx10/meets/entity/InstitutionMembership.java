package io.github.gugomesx10.meets.entity;

import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "meet_institution_membership",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_membership_user_institution",
                        columnNames = {"user_id", "institution_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class InstitutionMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InstitutionRole role;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
