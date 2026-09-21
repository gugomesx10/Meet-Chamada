package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.PresenceEvidence;
import io.github.gugomesx10.meets.entity.enums.EvidenceSource;
import io.github.gugomesx10.meets.entity.enums.PresenceEvidenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PresenceEvidenceRepository extends JpaRepository<PresenceEvidence, UUID> {
    List<PresenceEvidence> findAllByStudentIdAndClassSessionId(
            UUID studentId,
            UUID classSessionId
    );
    List<PresenceEvidence> findAllByClassSessionId(UUID classSessionId);
    List<PresenceEvidence> findAllByStudentIdAndClassSessionIdAndType(
            UUID studentId,
            UUID classSessionId,
            PresenceEvidenceType type
    );
    List<PresenceEvidence> findAllBySessionBlockId(UUID sessionBlockId);
    boolean existsBySourceAndExternalReference(
            EvidenceSource source,
            String externalReference
    );
}