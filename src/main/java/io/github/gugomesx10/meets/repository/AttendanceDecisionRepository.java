package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.AttendanceDecision;
import io.github.gugomesx10.meets.entity.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceDecisionRepository extends JpaRepository<AttendanceDecision, UUID> {
    Optional<AttendanceDecision> findByStudentIdAndClassSessionId(
            UUID studentId,
            UUID classSessionId
    );
    List<AttendanceDecision> findAllByClassSessionId(UUID classSessionId);
    List<AttendanceDecision> findAllByClassSessionIdAndStatus(
            UUID classSessionId,
            AttendanceStatus status
    );
    boolean existsByStudentIdAndClassSessionId(
            UUID studentId,
            UUID classSessionId
    );
}