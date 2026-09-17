package io.github.gugomesx10.meets.entity;

import io.github.gugomesx10.meets.entity.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meet_attendance_review")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_decision_id", nullable = false)
    private AttendanceDecision attendanceDecision;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 30)
    private AttendanceStatus previousStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private AttendanceStatus newStatus;
    @Column(nullable = false, length = 2000)
    private String reason;
    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private Instant reviewedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (reviewedAt == null) {
            reviewedAt = now;
        }
    }
}