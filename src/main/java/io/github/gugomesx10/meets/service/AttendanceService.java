package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.AttendanceDecision;
import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.CourseMembership;
import io.github.gugomesx10.meets.entity.PresenceEvidence;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.AttendanceDecisionSource;
import io.github.gugomesx10.meets.entity.enums.AttendanceStatus;
import io.github.gugomesx10.meets.entity.enums.ClassSessionStatus;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import io.github.gugomesx10.meets.entity.enums.PresenceEvidenceType;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.AttendanceDecisionRepository;
import io.github.gugomesx10.meets.repository.ClassSessionRepository;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.PresenceEvidenceRepository;
import io.github.gugomesx10.meets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private static final Set<PresenceEvidenceType>
            ACTIVE_EVIDENCE_TYPES =
            EnumSet.of(
                    PresenceEvidenceType.CHECK_IN,
                    PresenceEvidenceType.CHECK_OUT,
                    PresenceEvidenceType.POLL_RESPONSE,
                    PresenceEvidenceType.ACTIVITY_RESPONSE,
                    PresenceEvidenceType.CHAT_INTERACTION,
                    PresenceEvidenceType.REACTION,
                    PresenceEvidenceType.ORAL_INTERACTION
            );

    private final AttendanceDecisionRepository attendanceDecisionRepository;
    private final PresenceEvidenceRepository presenceEvidenceRepository;
    private final UserRepository userRepository;
    private final ClassSessionRepository classSessionRepository;
    private final CourseMembershipRepository courseMembershipRepository;
    private final AuditService auditService;
    private final AuthorizationService authorizationService;

    @Transactional
    public AttendanceDecision evaluate(
            UUID studentId,
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

        return evaluateInternal(
                studentId,
                classSession
        );
    }
    @Transactional
    public List<AttendanceDecision> evaluateSession(
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

        return courseMembershipRepository
                .findAllByCourseId(
                        classSession
                                .getCourse()
                                .getId()
                )
                .stream()
                .filter(membership ->
                        membership.getRole()
                                == CourseRole.STUDENT
                )
                .map(CourseMembership::getUser)
                .map(student ->
                        evaluateInternal(
                                student.getId(),
                                classSession
                        )
                )
                .toList();
    }
    @Transactional(readOnly = true)
    public AttendanceDecision findByStudentAndSession(
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

        return attendanceDecisionRepository
                .findByStudentIdAndClassSessionId(
                        studentId,
                        classSessionId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Decisão de presença não encontrada."
                        )
                );
    }
    @Transactional(readOnly = true)
    public List<AttendanceDecision> findBySession(
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

        return attendanceDecisionRepository
                .findAllByClassSessionId(
                        classSessionId
                );
    }
    @Transactional(readOnly = true)
    public List<AttendanceDecision> findReviewRequired(
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

        return attendanceDecisionRepository
                .findAllByClassSessionIdAndStatus(
                        classSessionId,
                        AttendanceStatus.REVIEW_REQUIRED
                );
    }

    private AttendanceDecision evaluateInternal(
            UUID studentId,
            ClassSession classSession
    ) {

        User student =
                userRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Aluno não encontrado."
                                )
                        );

        validateStudent(
                studentId,
                classSession
        );

        UUID classSessionId =
                classSession.getId();

        AttendanceDecision decision =
                attendanceDecisionRepository
                        .findByStudentIdAndClassSessionId(
                                studentId,
                                classSessionId
                        )
                        .orElseGet(() -> {

                            AttendanceDecision created =
                                    new AttendanceDecision();

                            created.setStudent(student);

                            created.setClassSession(
                                    classSession
                            );

                            return created;
                        });

        if (decision.getDecisionSource() != null
                && decision.getDecisionSource()
                != AttendanceDecisionSource.SYSTEM) {

            return decision;
        }

        if (classSession.getStatus()
                != ClassSessionStatus.COMPLETED) {

            decision.setStatus(
                    AttendanceStatus.PENDING
            );

            decision.setDecisionSource(
                    AttendanceDecisionSource.SYSTEM
            );

            decision.setDecidedBy(null);
            decision.setDecidedAt(null);

            decision.setReason(
                    "A aula ainda não foi concluída."
            );

            return attendanceDecisionRepository.save(
                    decision
            );
        }

        List<PresenceEvidence> evidences =
                presenceEvidenceRepository
                        .findAllByStudentIdAndClassSessionId(
                                studentId,
                                classSessionId
                        );

        AttendanceStatus status;
        String reason;

        boolean teacherConfirmation =
                evidences.stream()
                        .anyMatch(evidence ->
                                evidence.getType()
                                        == PresenceEvidenceType.TEACHER_CONFIRMATION
                        );

        boolean meetingSession =
                evidences.stream()
                        .anyMatch(evidence ->
                                evidence.getType()
                                        == PresenceEvidenceType.MEETING_SESSION
                        );

        long activeEvidenceCount =
                evidences.stream()
                        .filter(evidence ->
                                ACTIVE_EVIDENCE_TYPES.contains(
                                        evidence.getType()
                                )
                        )
                        .count();

        if (teacherConfirmation) {

            status =
                    AttendanceStatus.CONFIRMED;

            reason =
                    "Presença confirmada manualmente pelo professor.";

        } else if (meetingSession
                && activeEvidenceCount >= 1) {

            status =
                    AttendanceStatus.CONFIRMED;

            reason =
                    "Participação confirmada por conexão à aula e evidência ativa.";

        } else if (activeEvidenceCount >= 2) {

            status =
                    AttendanceStatus.CONFIRMED;

            reason =
                    "Participação confirmada por múltiplas evidências ativas.";

        } else if (meetingSession) {

            status =
                    AttendanceStatus.REVIEW_REQUIRED;

            reason =
                    "Foi identificada conexão à aula, mas não há evidências adicionais suficientes.";

        } else if (activeEvidenceCount == 1) {

            status =
                    AttendanceStatus.REVIEW_REQUIRED;

            reason =
                    "Foi identificada apenas uma evidência ativa de participação.";

        } else {

            status =
                    AttendanceStatus.ABSENT;

            reason =
                    "Nenhuma evidência de presença ou participação foi encontrada.";
        }

        decision.setStatus(status);

        decision.setDecisionSource(
                AttendanceDecisionSource.SYSTEM
        );

        decision.setDecidedBy(null);

        decision.setDecidedAt(
                Instant.now()
        );

        decision.setReason(reason);

        AttendanceDecision saved =
                attendanceDecisionRepository.save(
                        decision
                );

        auditService.registerSystemEvent(
                "ATTENDANCE_EVALUATED",
                "AttendanceDecision",
                saved.getId(),
                "Presença avaliada automaticamente como "
                        + status.name()
                        + "."
        );

        return saved;
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