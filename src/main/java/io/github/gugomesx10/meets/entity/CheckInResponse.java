package io.github.gugomesx10.meets.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "meet_check_in_response",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_check_in_response_window_student",
                        columnNames = {"check_in_window_id", "student_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CheckInResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_in_window_id", nullable = false)
    private CheckInWindow checkInWindow;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    @Column(name = "responded_at", nullable = false, updatable = false)
    private Instant respondedAt;
    @Column(nullable = false)
    private boolean valid;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}