package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.SessionBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SessionBlockRepository extends JpaRepository<SessionBlock, UUID> {
    List<SessionBlock> findAllByClassSessionIdOrderByStartTimeAsc(
            UUID classSessionId
    );
    List<SessionBlock> findAllByInstructorId(UUID instructorId);
}