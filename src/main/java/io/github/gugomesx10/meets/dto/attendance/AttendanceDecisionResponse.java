package io.github.gugomesx10.meets.dto.attendance;

import io.github.gugomesx10.meets.entity.AttendanceDecision;
import io.github.gugomesx10.meets.entity.enums.AttendanceDecisionSource;
import io.github.gugomesx10.meets.entity.enums.AttendanceStatus;
import java.time.Instant;
import java.util.UUID;

public record AttendanceDecisionResponse(
        UUID id,
        UUID studentId,
        UUID classSessionId,
        AttendanceStatus status,
        AttendanceDecisionSource decisionSource,
        UUID decidedById,
        String reason,
        Instant decidedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static AttendanceDecisionResponse from(
            AttendanceDecision decision
    ) {
        return new AttendanceDecisionResponse(
                decision.getId(),
                decision.getStudent().getId(),
                decision.getClassSession().getId(),
                decision.getStatus(),
                decision.getDecisionSource(),

                decision.getDecidedBy() != null
                        ? decision.getDecidedBy().getId()
                        : null,

                decision.getReason(),
                decision.getDecidedAt(),
                decision.getCreatedAt(),
                decision.getUpdatedAt()
        );
    }
}