package io.github.gugomesx10.meets.entity;

import io.github.gugomesx10.meets.entity.enums.AttendanceDecisionSource;
import io.github.gugomesx10.meets.entity.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "meet_attendance_decision",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attendance_student_session",
                        columnNames = {"student_id", "class_session_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AttendanceDecision {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_session_id", nullable = false)
    private ClassSession classSession;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttendanceStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "decision_source", nullable = false, length = 30)
    private AttendanceDecisionSource decisionSource;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private User decidedBy;
    @Column(length = 2000)
    private String reason;
    @Column(name = "decided_at")
    private Instant decidedAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}