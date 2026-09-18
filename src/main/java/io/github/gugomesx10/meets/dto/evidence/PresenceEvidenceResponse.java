package io.github.gugomesx10.meets.dto.evidence;

import io.github.gugomesx10.meets.entity.PresenceEvidence;
import io.github.gugomesx10.meets.entity.enums.EvidenceSource;
import io.github.gugomesx10.meets.entity.enums.PresenceEvidenceType;
import java.time.Instant;
import java.util.UUID;

public record PresenceEvidenceResponse(
        UUID id,
        UUID studentId,
        UUID classSessionId,
        UUID sessionBlockId,
        PresenceEvidenceType type,
        EvidenceSource source,
        Instant occurredAt,
        String details,
        Instant createdAt
) {

    public static PresenceEvidenceResponse from(
            PresenceEvidence evidence
    ) {
        return new PresenceEvidenceResponse(
                evidence.getId(),
                evidence.getStudent().getId(),
                evidence.getClassSession().getId(),

                evidence.getSessionBlock() != null
                        ? evidence.getSessionBlock().getId()
                        : null,

                evidence.getType(),
                evidence.getSource(),
                evidence.getOccurredAt(),
                evidence.getDetails(),
                evidence.getCreatedAt()
        );
    }
}