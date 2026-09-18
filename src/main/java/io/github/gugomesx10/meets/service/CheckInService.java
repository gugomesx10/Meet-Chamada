package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.CheckInResponse;
import io.github.gugomesx10.meets.entity.CheckInWindow;
import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.CourseMembership;
import io.github.gugomesx10.meets.entity.SessionBlock;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.CheckInStatus;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import io.github.gugomesx10.meets.entity.enums.EvidenceSource;
import io.github.gugomesx10.meets.entity.enums.PresenceEvidenceType;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.CheckInResponseRepository;
import io.github.gugomesx10.meets.repository.CheckInWindowRepository;
import io.github.gugomesx10.meets.repository.ClassSessionRepository;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.SessionBlockRepository;
import io.github.gugomesx10.meets.repository.UserRepository;
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

        if (duration == null
                || duration.isZero()
                || duration.isNegative()) {

            throw new BusinessRuleException(
                    "A duração do check-in deve ser maior que zero."
            );
        }

        ClassSession classSession =
                classSessionRepository
                        .findById(classSessionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sessão de aula não encontrada."
                                )
                        );

        User openedBy =
                userRepository
                        .findById(openedById)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Usuário responsável pelo check-in não encontrado."
                                )
                        );

        validateInstructor(
                openedById,
                classSession
        );

        SessionBlock sessionBlock = null;

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

        Instant now = Instant.now();

        CheckInWindow window =
                new CheckInWindow();

        window.setClassSession(classSession);
        window.setSessionBlock(sessionBlock);
        window.setOpenedBy(openedBy);
        window.setOpenedAt(now);
        window.setClosesAt(
                now.plus(duration)
        );
        window.setStatus(
                CheckInStatus.OPEN
        );

        CheckInWindow saved =
                checkInWindowRepository.save(
                        window
                );

        auditService.register(
                openedBy,
                "CHECK_IN_OPENED",
                "CheckInWindow",
                saved.getId(),
                "Janela de check-in aberta."
        );

        return saved;
    }
    @Transactional(noRollbackFor = ConflictException.class)
    public CheckInResponse respond(
            UUID checkInWindowId,
            UUID studentId
    ) {

        CheckInWindow window =
                checkInWindowRepository
                        .findById(checkInWindowId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Check-in não encontrado."
                                )
                        );

        User student =
                userRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Aluno não encontrado."
                                )
                        );

        if (window.getStatus()
                != CheckInStatus.OPEN) {

            throw new ConflictException(
                    "O check-in não está aberto."
            );
        }

        Instant now = Instant.now();

        if (now.isAfter(window.getClosesAt())) {

            window.setStatus(
                    CheckInStatus.CLOSED
            );

            checkInWindowRepository.save(window);

            throw new ConflictException(
                    "O período para responder ao check-in foi encerrado."
            );
        }

        validateStudent(
                studentId,
                window.getClassSession()
        );

        if (checkInResponseRepository
                .existsByCheckInWindowIdAndStudentId(
                        checkInWindowId,
                        studentId
                )) {

            throw new ConflictException(
                    "O aluno já respondeu a este check-in."
            );
        }

        CheckInResponse response =
                new CheckInResponse();

        response.setCheckInWindow(window);
        response.setStudent(student);
        response.setRespondedAt(now);
        response.setValid(true);

        CheckInResponse saved =
                checkInResponseRepository.save(
                        response
                );

        presenceEvidenceService.register(
                student,
                window.getClassSession(),
                window.getSessionBlock(),
                PresenceEvidenceType.CHECK_IN,
                EvidenceSource.INTERNAL,
                now,
                "Resposta válida ao check-in."
        );

        auditService.register(
                student,
                "CHECK_IN_RESPONDED",
                "CheckInResponse",
                saved.getId(),
                "Aluno respondeu ao check-in."
        );

        return saved;
    }
    @Transactional
    public CheckInWindow closeCheckIn(
            UUID checkInWindowId,
            UUID userId
    ) {

        CheckInWindow window =
                findRequired(
                        checkInWindowId
                );

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Usuário não encontrado."
                                )
                        );

        validateInstructor(
                userId,
                window.getClassSession()
        );

        if (window.getStatus()
                != CheckInStatus.OPEN) {

            throw new ConflictException(
                    "Somente um check-in aberto pode ser fechado."
            );
        }

        window.setStatus(
                CheckInStatus.CLOSED
        );

        CheckInWindow saved =
                checkInWindowRepository.save(
                        window
                );

        auditService.register(
                user,
                "CHECK_IN_CLOSED",
                "CheckInWindow",
                saved.getId(),
                "Janela de check-in encerrada."
        );

        return saved;
    }
    @Transactional
    public CheckInWindow cancelCheckIn(
            UUID checkInWindowId,
            UUID userId
    ) {

        CheckInWindow window =
                findRequired(
                        checkInWindowId
                );

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Usuário não encontrado."
                                )
                        );

        validateInstructor(
                userId,
                window.getClassSession()
        );

        if (window.getStatus()
                != CheckInStatus.OPEN) {

            throw new ConflictException(
                    "Somente um check-in aberto pode ser cancelado."
            );
        }

        window.setStatus(
                CheckInStatus.CANCELLED
        );

        CheckInWindow saved =
                checkInWindowRepository.save(
                        window
                );

        auditService.register(
                user,
                "CHECK_IN_CANCELLED",
                "CheckInWindow",
                saved.getId(),
                "Janela de check-in cancelada."
        );

        return saved;
    }
    @Transactional(readOnly = true)
    public List<CheckInWindow> findBySession(
            UUID classSessionId
    ) {

        if (!classSessionRepository.existsById(
                classSessionId
        )) {

            throw new ResourceNotFoundException(
                    "Sessão de aula não encontrada."
            );
        }

        return checkInWindowRepository
                .findAllByClassSessionId(
                        classSessionId
                );
    }
    @Transactional(readOnly = true)
    public List<CheckInResponse> findResponses(
            UUID checkInWindowId
    ) {

        if (!checkInWindowRepository.existsById(
                checkInWindowId
        )) {

            throw new ResourceNotFoundException(
                    "Check-in não encontrado."
            );
        }

        return checkInResponseRepository
                .findAllByCheckInWindowId(
                        checkInWindowId
                );
    }

    private CheckInWindow findRequired(
            UUID checkInWindowId
    ) {

        return checkInWindowRepository
                .findById(checkInWindowId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Check-in não encontrado."
                        )
                );
    }

    private void validateInstructor(
            UUID userId,
            ClassSession classSession
    ) {

        CourseMembership membership =
                courseMembershipRepository
                        .findByUserIdAndCourseId(
                                userId,
                                classSession
                                        .getCourse()
                                        .getId()
                        )
                        .orElseThrow(() ->
                                new ForbiddenOperationException(
                                        "O usuário não pertence ao curso."
                                )
                        );

        if (membership.getRole()
                != CourseRole.INSTRUCTOR) {

            throw new ForbiddenOperationException(
                    "Somente um instrutor do curso pode gerenciar o check-in."
            );
        }
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
                                new ForbiddenOperationException(
                                        "O aluno não pertence ao curso."
                                )
                        );

        if (membership.getRole()
                != CourseRole.STUDENT) {

            throw new ForbiddenOperationException(
                    "O usuário informado não é aluno deste curso."
            );
        }
    }
}