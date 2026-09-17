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
import io.github.gugomesx10.meets.repository.AttendanceDecisionRepository;
import io.github.gugomesx10.meets.repository.ClassSessionRepository;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private static final EnumSet<PresenceEvidenceType> ACTIVE_EVIDENCE_TYPES =
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
    private final ClassSessionRepository classSessionRepository;
    private final UserRepository userRepository;
    private final CourseMembershipRepository courseMembershipRepository;

    private final PresenceEvidenceService presenceEvidenceService;
    private final AuditService auditService;

    @Transactional
    public AttendanceDecision evaluate(
            UUID studentId,
            UUID classSessionId
    ) {

        User student = userRepository
                .findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Aluno não encontrado."
                        )
                );

        ClassSession classSession = classSessionRepository
                .findById(classSessionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sessão de aula não encontrada."
                        )
                );

        validateStudentMembership(
                studentId,
                classSession.getCourse().getId()
        );

        AttendanceDecision decision =
                attendanceDecisionRepository
                        .findByStudentIdAndClassSessionId(
                                studentId,
                                classSessionId
                        )
                        .orElseGet(AttendanceDecision::new);

        if (decision.getId() != null
                && decision.getDecisionSource() != AttendanceDecisionSource.SYSTEM) {

            return decision;
        }

        decision.setStudent(student);
        decision.setClassSession(classSession);
        if (classSession.getStatus() != ClassSessionStatus.COMPLETED) {

            decision.setStatus(AttendanceStatus.PENDING);
            decision.setDecisionSource(AttendanceDecisionSource.SYSTEM);
            decision.setDecidedBy(null);
            decision.setDecidedAt(null);
            decision.setReason(
                    "A sessão de aula ainda não foi concluída."
            );

            return attendanceDecisionRepository.save(decision);
        }

        List<PresenceEvidence> evidences =
                presenceEvidenceService.findByStudentAndSession(
                        studentId,
                        classSessionId
                );

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

        AttendanceStatus status;
        String reason;

        if (teacherConfirmation) {

            status = AttendanceStatus.CONFIRMED;

            reason =
                    "Presença confirmada por evidência registrada pelo professor.";

        } else if (meetingSession && activeEvidenceCount >= 1) {

            status = AttendanceStatus.CONFIRMED;

            reason =
                    "Participação confirmada por sessão de reunião combinada com evidência ativa.";

        } else if (activeEvidenceCount >= 2) {

            status = AttendanceStatus.CONFIRMED;

            reason =
                    "Participação confirmada por múltiplas evidências ativas durante a aula.";

        } else if (meetingSession) {

            status = AttendanceStatus.REVIEW_REQUIRED;

            reason =
                    "Foi encontrada evidência de conexão à reunião, mas não há outras evidências suficientes de participação.";

        } else if (activeEvidenceCount == 1) {

            status = AttendanceStatus.REVIEW_REQUIRED;

            reason =
                    "Foi encontrada apenas uma evidência ativa de participação. É necessária revisão.";

        } else {

            status = AttendanceStatus.ABSENT;

            reason =
                    "Não foram encontradas evidências suficientes de participação após o encerramento da sessão.";
        }

        decision.setStatus(status);
        decision.setDecisionSource(AttendanceDecisionSource.SYSTEM);
        decision.setDecidedBy(null);
        decision.setDecidedAt(Instant.now());
        decision.setReason(reason);

        AttendanceDecision savedDecision =
                attendanceDecisionRepository.save(decision);

        auditService.registerSystemEvent(
                "ATTENDANCE_EVALUATED",
                "AttendanceDecision",
                savedDecision.getId(),
                "Presença avaliada automaticamente. Resultado: "
                        + status.name()
        );

        return savedDecision;
    }
    @Transactional
    public List<AttendanceDecision> evaluateSession(
            UUID classSessionId
    ) {

        ClassSession classSession = classSessionRepository
                .findById(classSessionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sessão de aula não encontrada."
                        )
                );

        List<CourseMembership> memberships =
                courseMembershipRepository
                        .findAllByCourseId(
                                classSession.getCourse().getId()
                        );

        return memberships.stream()
                .filter(membership ->
                        membership.getRole() == CourseRole.STUDENT
                )
                .map(membership ->
                        evaluate(
                                membership.getUser().getId(),
                                classSessionId
                        )
                )
                .toList();
    }
    @Transactional(readOnly = true)
    public AttendanceDecision findByStudentAndSession(
            UUID studentId,
            UUID classSessionId
    ) {

        return attendanceDecisionRepository
                .findByStudentIdAndClassSessionId(
                        studentId,
                        classSessionId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Decisão de presença não encontrada."
                        )
                );
    }
    @Transactional(readOnly = true)
    public List<AttendanceDecision> findBySession(
            UUID classSessionId
    ) {

        return attendanceDecisionRepository
                .findAllByClassSessionId(classSessionId);
    }
    @Transactional(readOnly = true)
    public List<AttendanceDecision> findReviewRequired(
            UUID classSessionId
    ) {

        return attendanceDecisionRepository
                .findAllByClassSessionIdAndStatus(
                        classSessionId,
                        AttendanceStatus.REVIEW_REQUIRED
                );
    }

    private void validateStudentMembership(
            UUID studentId,
            UUID courseId
    ) {

        CourseMembership membership =
                courseMembershipRepository
                        .findByUserIdAndCourseId(
                                studentId,
                                courseId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "O usuário não pertence ao curso."
                                )
                        );

        if (membership.getRole() != CourseRole.STUDENT) {
            throw new IllegalStateException(
                    "A avaliação de frequência só pode ser realizada para alunos."
            );
        }
    }
}