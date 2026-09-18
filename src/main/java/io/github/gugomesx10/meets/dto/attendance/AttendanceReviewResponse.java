package io.github.gugomesx10.meets.dto.attendance;

import io.github.gugomesx10.meets.entity.AttendanceReview;
import io.github.gugomesx10.meets.entity.enums.AttendanceStatus;
import java.time.Instant;
import java.util.UUID;

public record AttendanceReviewResponse(
        UUID id,
        UUID attendanceDecisionId,
        UUID reviewerId,
        AttendanceStatus previousStatus,
        AttendanceStatus newStatus,
        String reason,
        Instant reviewedAt,
        Instant createdAt

) {

    public static AttendanceReviewResponse from(
            AttendanceReview review
    ) {
        return new AttendanceReviewResponse(
                review.getId(),
                review.getAttendanceDecision().getId(),
                review.getReviewer().getId(),
                review.getPreviousStatus(),
                review.getNewStatus(),
                review.getReason(),
                review.getReviewedAt(),
                review.getCreatedAt()
        );
    }
}