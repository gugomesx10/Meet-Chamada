package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassSessionRepository extends JpaRepository<ClassSession, UUID> {
    List<ClassSession> findAllByCourseId(UUID courseId);
    List<ClassSession> findAllByCourseIdOrderBySessionDateAsc(UUID courseId);
    Optional<ClassSession> findByCourseIdAndSessionDate(
            UUID courseId,
            LocalDate sessionDate
    );
}