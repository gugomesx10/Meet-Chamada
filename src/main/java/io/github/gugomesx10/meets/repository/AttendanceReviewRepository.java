package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.AttendanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AttendanceReviewRepository extends JpaRepository<AttendanceReview, UUID> {
    List<AttendanceReview> findAllByAttendanceDecisionIdOrderByReviewedAtAsc(
            UUID attendanceDecisionId
    );
    List<AttendanceReview> findAllByReviewerId(UUID reviewerId);
}