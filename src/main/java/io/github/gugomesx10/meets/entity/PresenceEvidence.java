package io.github.gugomesx10.meets.entity;

import io.github.gugomesx10.meets.entity.enums.EvidenceSource;
import io.github.gugomesx10.meets.entity.enums.PresenceEvidenceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name="meet_presence_evidence")
@Getter
@Setter
@NoArgsConstructor
public class PresenceEvidence {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_session_id", nullable = false)
    private ClassSession classSession;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_block_id")
    private SessionBlock sessionBlock;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PresenceEvidenceType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EvidenceSource source;
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;
    @Column(length = 1000)
    private String details;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if(createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
