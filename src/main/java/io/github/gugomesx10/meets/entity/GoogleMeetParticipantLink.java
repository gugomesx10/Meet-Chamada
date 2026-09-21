package io.github.gugomesx10.meets.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meet_google_meet_participant_link")
@Getter
@Setter
@NoArgsConstructor
public class GoogleMeetParticipantLink {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;
    @Column(
            name = "google_meet_user_name",
            nullable = false,
            unique = true,
            length = 255
    )
    private String googleMeetUserName;
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;
    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @PrePersist
    void prePersist() {

        Instant now =
                Instant.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {

        updatedAt =
                Instant.now();
    }
}