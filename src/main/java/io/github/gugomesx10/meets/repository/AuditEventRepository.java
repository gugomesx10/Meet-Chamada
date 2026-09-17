package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    List<AuditEvent> findAllByActorIdOrderByOccurredAtDesc(UUID actorId);
    List<AuditEvent> findAllByEntityTypeAndEntityIdOrderByOccurredAtDesc(
            String entityType,
            UUID entityId
    );
}