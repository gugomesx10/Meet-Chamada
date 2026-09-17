package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.CourseMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseMembershipRepository extends JpaRepository<CourseMembership, UUID> {
    Optional<CourseMembership> findByUserIdAndCourseId(
            UUID userId,
            UUID courseId
    );
    List<CourseMembership> findAllByCourseId(UUID courseId);
    List<CourseMembership> findAllByUserId(UUID userId);
    boolean existsByUserIdAndCourseId(
            UUID userId,
            UUID courseId
    );
}