package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.AuditEvent;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    @Transactional
    public AuditEvent register(
            User actor,
            String action,
            String entityType,
            UUID entityId,
            String details
    ) {
        AuditEvent event = new AuditEvent();

        event.setActor(actor);
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setDetails(details);
        event.setOccurredAt(Instant.now());

        return auditEventRepository.save(event);
    }
    @Transactional
    public AuditEvent registerSystemEvent(
            String action,
            String entityType,
            UUID entityId,
            String details
    ) {
        return register(
                null,
                action,
                entityType,
                entityId,
                details
        );
    }
    @Transactional(readOnly = true)
    public List<AuditEvent> findByEntity(
            String entityType,
            UUID entityId
    ) {
        return auditEventRepository
                .findAllByEntityTypeAndEntityIdOrderByOccurredAtDesc(
                        entityType,
                        entityId
                );
    }
    @Transactional(readOnly = true)
    public List<AuditEvent> findByActor(UUID actorId) {
        return auditEventRepository
                .findAllByActorIdOrderByOccurredAtDesc(actorId);
    }
}