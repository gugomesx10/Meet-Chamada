package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.PresenceEvidence;
import io.github.gugomesx10.meets.entity.SessionBlock;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.EvidenceSource;
import io.github.gugomesx10.meets.entity.enums.PresenceEvidenceType;
import io.github.gugomesx10.meets.repository.PresenceEvidenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresenceEvidenceService {

    private final PresenceEvidenceRepository presenceEvidenceRepository;
    @Transactional
    public PresenceEvidence register(
            User student,
            ClassSession classSession,
            SessionBlock sessionBlock,
            PresenceEvidenceType type,
            EvidenceSource source,
            Instant occurredAt,
            String details
    ) {

        PresenceEvidence evidence = new PresenceEvidence();

        evidence.setStudent(student);
        evidence.setClassSession(classSession);
        evidence.setSessionBlock(sessionBlock);
        evidence.setType(type);
        evidence.setSource(source);

        evidence.setOccurredAt(
                occurredAt != null
                        ? occurredAt
                        : Instant.now()
        );

        evidence.setDetails(details);

        return presenceEvidenceRepository.save(evidence);
    }
    @Transactional(readOnly = true)
    public List<PresenceEvidence> findByStudentAndSession(
            UUID studentId,
            UUID classSessionId
    ) {
        return presenceEvidenceRepository
                .findAllByStudentIdAndClassSessionId(
                        studentId,
                        classSessionId
                );
    }
    @Transactional(readOnly = true)
    public List<PresenceEvidence> findBySession(
            UUID classSessionId
    ) {
        return presenceEvidenceRepository
                .findAllByClassSessionId(classSessionId);
    }
    @Transactional(readOnly = true)
    public List<PresenceEvidence> findByStudentSessionAndType(
            UUID studentId,
            UUID classSessionId,
            PresenceEvidenceType type
    ) {
        return presenceEvidenceRepository
                .findAllByStudentIdAndClassSessionIdAndType(
                        studentId,
                        classSessionId,
                        type
                );
    }
    @Transactional(readOnly = true)
    public List<PresenceEvidence> findBySessionBlock(
            UUID sessionBlockId
    ) {
        return presenceEvidenceRepository
                .findAllBySessionBlockId(sessionBlockId);
    }
}