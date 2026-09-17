package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.*;
import io.github.gugomesx10.meets.entity.enums.CheckInStatus;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import io.github.gugomesx10.meets.entity.enums.EvidenceSource;
import io.github.gugomesx10.meets.entity.enums.PresenceEvidenceType;
import io.github.gugomesx10.meets.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInWindowRepository checkInWindowRepository;
    private final CheckInResponseRepository checkInResponseRepository;

    private final ClassSessionRepository classSessionRepository;
    private final SessionBlockRepository sessionBlockRepository;
    private final UserRepository userRepository;
    private final CourseMembershipRepository courseMembershipRepository;

    private final PresenceEvidenceService presenceEvidenceService;
    private final AuditService auditService;

    @Transactional
    public CheckInWindow openCheckIn(
            UUID classSessionId,
            UUID sessionBlockId,
            UUID openedById,
            Duration duration
    ) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(
                    "A duração do check-in deve ser maior que zero."
            );
        }

        ClassSession classSession = classSessionRepository
                .findById(classSessionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sessão de aula não encontrada."
                        )
                );

        User openedBy = userRepository
                .findById(openedById)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário responsável pelo check-in não encontrado."
                        )
                );

        validateInstructor(
                openedBy.getId(),
                classSession.getCourse().getId()
        );

        SessionBlock sessionBlock = null;

        if (sessionBlockId != null) {
            sessionBlock = sessionBlockRepository
                    .findById(sessionBlockId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Bloco da sessão não encontrado."
                            )
                    );

            if (!sessionBlock.getClassSession().getId()
                    .equals(classSession.getId())) {

                throw new IllegalArgumentException(
                        "O bloco informado não pertence à sessão de aula."
                );
            }
        }

        Instant now = Instant.now();

        CheckInWindow window = new CheckInWindow();

        window.setClassSession(classSession);
        window.setSessionBlock(sessionBlock);
        window.setOpenedBy(openedBy);
        window.setOpenedAt(now);
        window.setClosesAt(now.plus(duration));
        window.setStatus(CheckInStatus.OPEN);

        CheckInWindow savedWindow =
                checkInWindowRepository.save(window);

        auditService.register(
                openedBy,
                "CHECK_IN_OPENED",
                "CheckInWindow",
                savedWindow.getId(),
                "Janela de check-in aberta."
        );

        return savedWindow;
    }
    @Transactional
    public CheckInResponse respond(
            UUID checkInWindowId,
            UUID studentId
    ) {
        CheckInWindow window = checkInWindowRepository
                .findById(checkInWindowId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Janela de check-in não encontrada."
                        )
                );

        User student = userRepository
                .findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Aluno não encontrado."
                        )
                );

        if (window.getStatus() != CheckInStatus.OPEN) {
            throw new IllegalStateException(
                    "O check-in não está aberto."
            );
        }

        Instant now = Instant.now();

        if (now.isAfter(window.getClosesAt())) {
            window.setStatus(CheckInStatus.CLOSED);
            checkInWindowRepository.save(window);

            throw new IllegalStateException(
                    "O prazo para responder ao check-in terminou."
            );
        }

        UUID courseId =
                window.getClassSession().getCourse().getId();

        validateStudent(studentId, courseId);

        boolean alreadyResponded =
                checkInResponseRepository
                        .existsByCheckInWindowIdAndStudentId(
                                checkInWindowId,
                                studentId
                        );

        if (alreadyResponded) {
            throw new IllegalStateException(
                    "O aluno já respondeu a este check-in."
            );
        }

        CheckInResponse response = new CheckInResponse();

        response.setCheckInWindow(window);
        response.setStudent(student);
        response.setRespondedAt(now);
        response.setValid(true);

        CheckInResponse savedResponse =
                checkInResponseRepository.save(response);

        presenceEvidenceService.register(
                student,
                window.getClassSession(),
                window.getSessionBlock(),
                PresenceEvidenceType.CHECK_IN,
                EvidenceSource.INTERNAL,
                now,
                "Check-in confirmado pelo aluno."
        );

        auditService.register(
                student,
                "CHECK_IN_RESPONDED",
                "CheckInResponse",
                savedResponse.getId(),
                "Aluno respondeu ao check-in."
        );

        return savedResponse;
    }
    @Transactional
    public CheckInWindow closeCheckIn(
            UUID checkInWindowId,
            UUID closedById
    ) {
        CheckInWindow window = getWindow(checkInWindowId);

        User closedBy = userRepository
                .findById(closedById)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado."
                        )
                );

        validateInstructor(
                closedBy.getId(),
                window.getClassSession().getCourse().getId()
        );

        if (window.getStatus() != CheckInStatus.OPEN) {
            throw new IllegalStateException(
                    "Somente um check-in aberto pode ser encerrado."
            );
        }

        window.setStatus(CheckInStatus.CLOSED);

        CheckInWindow savedWindow =
                checkInWindowRepository.save(window);

        auditService.register(
                closedBy,
                "CHECK_IN_CLOSED",
                "CheckInWindow",
                savedWindow.getId(),
                "Janela de check-in encerrada."
        );

        return savedWindow;
    }
    @Transactional
    public CheckInWindow cancelCheckIn(
            UUID checkInWindowId,
            UUID cancelledById
    ) {
        CheckInWindow window = getWindow(checkInWindowId);

        User cancelledBy = userRepository
                .findById(cancelledById)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado."
                        )
                );

        validateInstructor(
                cancelledBy.getId(),
                window.getClassSession().getCourse().getId()
        );

        if (window.getStatus() != CheckInStatus.OPEN) {
            throw new IllegalStateException(
                    "Somente um check-in aberto pode ser cancelado."
            );
        }

        window.setStatus(CheckInStatus.CANCELLED);

        CheckInWindow savedWindow =
                checkInWindowRepository.save(window);

        auditService.register(
                cancelledBy,
                "CHECK_IN_CANCELLED",
                "CheckInWindow",
                savedWindow.getId(),
                "Janela de check-in cancelada."
        );

        return savedWindow;
    }
    @Transactional(readOnly = true)
    public List<CheckInWindow> findBySession(
            UUID classSessionId
    ) {
        return checkInWindowRepository
                .findAllByClassSessionId(classSessionId);
    }
    @Transactional(readOnly = true)
    public List<CheckInResponse> findResponses(
            UUID checkInWindowId
    ) {
        return checkInResponseRepository
                .findAllByCheckInWindowId(checkInWindowId);
    }
    private CheckInWindow getWindow(UUID id) {
        return checkInWindowRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Janela de check-in não encontrada."
                        )
                );
    }
    private void validateInstructor(
            UUID userId,
            UUID courseId
    ) {
        CourseMembership membership =
                courseMembershipRepository
                        .findByUserIdAndCourseId(userId, courseId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "O usuário não pertence ao curso."
                                )
                        );

        if (membership.getRole() != CourseRole.INSTRUCTOR) {
            throw new IllegalStateException(
                    "Somente instrutores podem gerenciar check-ins."
            );
        }
    }
    private void validateStudent(
            UUID userId,
            UUID courseId
    ) {
        CourseMembership membership =
                courseMembershipRepository
                        .findByUserIdAndCourseId(userId, courseId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "O usuário não pertence ao curso."
                                )
                        );

        if (membership.getRole() != CourseRole.STUDENT) {
            throw new IllegalStateException(
                    "Somente alunos podem responder ao check-in."
            );
        }
    }
}