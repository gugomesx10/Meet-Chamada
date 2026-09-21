package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.CourseMembership;
import io.github.gugomesx10.meets.entity.PresenceEvidence;
import io.github.gugomesx10.meets.entity.SessionBlock;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import io.github.gugomesx10.meets.entity.enums.EvidenceSource;
import io.github.gugomesx10.meets.entity.enums.PresenceEvidenceType;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.ClassSessionRepository;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.PresenceEvidenceRepository;
import io.github.gugomesx10.meets.repository.SessionBlockRepository;
import io.github.gugomesx10.meets.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final ClassSessionRepository classSessionRepository;
    private final SessionBlockRepository sessionBlockRepository;
    private final CourseMembershipRepository courseMembershipRepository;
    private final AuditService auditService;
    private final AuthorizationService authorizationService;
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

        PresenceEvidence evidence =
                new PresenceEvidence();

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

        return presenceEvidenceRepository.save(
                evidence
        );
    }
    @Transactional
    public PresenceEvidence registerTeacherConfirmation(
            UUID studentId,
            UUID classSessionId,
            UUID sessionBlockId,
            User teacher,
            String details
    ) {

        User student =
                userRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Aluno não encontrado."
                                )
                        );

        ClassSession classSession =
                findClassSession(
                        classSessionId
                );

        validateStudent(
                studentId,
                classSession
        );

        authorizationService
                .requireCourseInstructorOrAdmin(
                        teacher,
                        classSession.getCourse()
                );

        SessionBlock sessionBlock =
                null;

        if (sessionBlockId != null) {

            sessionBlock =
                    sessionBlockRepository
                            .findById(sessionBlockId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Bloco da aula não encontrado."
                                    )
                            );

            if (!sessionBlock
                    .getClassSession()
                    .getId()
                    .equals(classSessionId)) {

                throw new BusinessRuleException(
                        "O bloco informado não pertence à sessão de aula."
                );
            }
        }

        PresenceEvidence evidence =
                register(
                        student,
                        classSession,
                        sessionBlock,
                        PresenceEvidenceType.TEACHER_CONFIRMATION,
                        EvidenceSource.TEACHER,
                        Instant.now(),
                        details
                );

        auditService.register(
                teacher,
                "TEACHER_CONFIRMATION_REGISTERED",
                "PresenceEvidence",
                evidence.getId(),
                "Confirmação manual de presença registrada para o aluno "
                        + student.getId()
                        + "."
        );

        return evidence;
    }
    @Transactional(readOnly = true)
    public List<PresenceEvidence> findByStudentAndSession(
            UUID studentId,
            UUID classSessionId,
            User currentUser
    ) {

        ClassSession classSession =
                findClassSession(
                        classSessionId
                );

        validateStudent(
                studentId,
                classSession
        );

        authorizationService
                .requireCourseInstructorOrAdminOrSelf(
                        currentUser,
                        classSession.getCourse(),
                        studentId
                );

        return presenceEvidenceRepository
                .findAllByStudentIdAndClassSessionId(
                        studentId,
                        classSessionId
                );
    }
    @Transactional(readOnly = true)
    public List<PresenceEvidence> findBySession(
            UUID classSessionId,
            User currentUser
    ) {

        ClassSession classSession =
                findClassSession(
                        classSessionId
                );

        authorizationService
                .requireCourseInstructorOrAdmin(
                        currentUser,
                        classSession.getCourse()
                );

        return presenceEvidenceRepository
                .findAllByClassSessionId(
                        classSessionId
                );
    }
    @Transactional(readOnly = true)
    public List<PresenceEvidence> findByStudentSessionAndType(
            UUID studentId,
            UUID classSessionId,
            PresenceEvidenceType type,
            User currentUser
    ) {

        ClassSession classSession =
                findClassSession(
                        classSessionId
                );

        validateStudent(
                studentId,
                classSession
        );

        authorizationService
                .requireCourseInstructorOrAdminOrSelf(
                        currentUser,
                        classSession.getCourse(),
                        studentId
                );

        return presenceEvidenceRepository
                .findAllByStudentIdAndClassSessionIdAndType(
                        studentId,
                        classSessionId,
                        type
                );
    }
    @Transactional(readOnly = true)
    public List<PresenceEvidence> findBySessionBlock(
            UUID sessionBlockId,
            User currentUser
    ) {

        SessionBlock sessionBlock =
                sessionBlockRepository
                        .findById(sessionBlockId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Bloco da aula não encontrado."
                                )
                        );

        authorizationService
                .requireCourseInstructorOrAdmin(
                        currentUser,
                        sessionBlock
                                .getClassSession()
                                .getCourse()
                );

        return presenceEvidenceRepository
                .findAllBySessionBlockId(
                        sessionBlockId
                );
    }

    private ClassSession findClassSession(
            UUID classSessionId
    ) {

        return classSessionRepository
                .findById(classSessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sessão de aula não encontrada."
                        )
                );
    }

    private void validateStudent(
            UUID studentId,
            ClassSession classSession
    ) {

        CourseMembership membership =
                courseMembershipRepository
                        .findByUserIdAndCourseId(
                                studentId,
                                classSession
                                        .getCourse()
                                        .getId()
                        )
                        .orElseThrow(() ->
                                new BusinessRuleException(
                                        "O usuário não pertence ao curso."
                                )
                        );

        if (membership.getRole()
                != CourseRole.STUDENT) {

            throw new BusinessRuleException(
                    "O usuário informado não é aluno deste curso."
            );
        }
    }
}