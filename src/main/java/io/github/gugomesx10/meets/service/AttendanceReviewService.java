package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.AttendanceDecision;
import io.github.gugomesx10.meets.entity.AttendanceReview;
import io.github.gugomesx10.meets.entity.CourseMembership;
import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.AttendanceDecisionSource;
import io.github.gugomesx10.meets.entity.enums.AttendanceStatus;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.AttendanceDecisionRepository;
import io.github.gugomesx10.meets.repository.AttendanceReviewRepository;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.InstitutionMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceReviewService {
    private final AttendanceReviewRepository attendanceReviewRepository;
    private final AttendanceDecisionRepository attendanceDecisionRepository;
    private final CourseMembershipRepository courseMembershipRepository;
    private final InstitutionMembershipRepository institutionMembershipRepository;
    private final AuditService auditService;
    private final AuthorizationService authorizationService;
    @Transactional
    public AttendanceReview review(
            UUID attendanceDecisionId,
            User reviewer,
            AttendanceStatus newStatus,
            String reason
    ) {

        AttendanceDecision decision =
                findDecision(
                        attendanceDecisionId
                );

        AttendanceDecisionSource source =
                resolveReviewerSource(
                        reviewer,
                        decision
                );

        validateNewStatus(
                newStatus
        );

        if (reason == null
                || reason.isBlank()) {

            throw new BusinessRuleException(
                    "O motivo da revisão é obrigatório."
            );
        }

        if (decision.getStatus()
                == AttendanceStatus.PENDING) {

            throw new ConflictException(
                    "Uma presença pendente ainda não pode ser revisada."
            );
        }

        AttendanceStatus previousStatus =
                decision.getStatus();

        if (previousStatus == newStatus) {

            throw new ConflictException(
                    "O novo status deve ser diferente do status atual."
            );
        }

        Instant now =
                Instant.now();

        AttendanceReview review =
                new AttendanceReview();

        review.setAttendanceDecision(
                decision
        );

        review.setReviewer(
                reviewer
        );

        review.setPreviousStatus(
                previousStatus
        );

        review.setNewStatus(
                newStatus
        );

        review.setReason(
                reason
        );

        review.setReviewedAt(
                now
        );

        AttendanceReview savedReview =
                attendanceReviewRepository.save(
                        review
                );

        decision.setStatus(
                newStatus
        );

        decision.setDecisionSource(
                source
        );

        decision.setDecidedBy(
                reviewer
        );

        decision.setDecidedAt(
                now
        );

        decision.setReason(
                reason
        );

        attendanceDecisionRepository.save(
                decision
        );

        auditService.register(
                reviewer,
                "ATTENDANCE_REVIEWED",
                "AttendanceDecision",
                decision.getId(),
                "Status alterado de "
                        + previousStatus.name()
                        + " para "
                        + newStatus.name()
                        + "."
        );

        return savedReview;
    }
    @Transactional(readOnly = true)
    public List<AttendanceReview> findHistory(
            UUID attendanceDecisionId,
            User currentUser
    ) {

        AttendanceDecision decision =
                findDecision(
                        attendanceDecisionId
                );

        authorizationService
                .requireCourseInstructorOrAdminOrSelf(
                        currentUser,
                        decision
                                .getClassSession()
                                .getCourse(),
                        decision
                                .getStudent()
                                .getId()
                );

        return attendanceReviewRepository
                .findAllByAttendanceDecisionIdOrderByReviewedAtAsc(
                        attendanceDecisionId
                );
    }
    @Transactional(readOnly = true)
    public List<AttendanceReview> findByReviewer(
            UUID reviewerId
    ) {

        return attendanceReviewRepository
                .findAllByReviewerId(
                        reviewerId
                );
    }

    private AttendanceDecision findDecision(
            UUID attendanceDecisionId
    ) {

        return attendanceDecisionRepository
                .findById(
                        attendanceDecisionId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Decisão de presença não encontrada."
                        )
                );
    }

    private void validateNewStatus(
            AttendanceStatus newStatus
    ) {

        if (newStatus == null) {

            throw new BusinessRuleException(
                    "O novo status é obrigatório."
            );
        }

        if (newStatus != AttendanceStatus.CONFIRMED
                && newStatus != AttendanceStatus.ABSENT
                && newStatus != AttendanceStatus.JUSTIFIED) {

            throw new BusinessRuleException(
                    "Uma revisão manual só pode resultar em "
                            + "CONFIRMED, ABSENT ou JUSTIFIED."
            );
        }
    }

    private AttendanceDecisionSource resolveReviewerSource(
            User reviewer,
            AttendanceDecision decision
    ) {

        var course =
                decision
                        .getClassSession()
                        .getCourse();

        authorizationService
                .requireCourseInstructorOrAdmin(
                        reviewer,
                        course
                );

        CourseMembership courseMembership =
                courseMembershipRepository
                        .findByUserIdAndCourseId(
                                reviewer.getId(),
                                course.getId()
                        )
                        .orElse(null);

        if (courseMembership != null
                && courseMembership.getRole()
                == CourseRole.INSTRUCTOR) {

            return AttendanceDecisionSource.TEACHER;
        }

        InstitutionMembership institutionMembership =
                institutionMembershipRepository
                        .findByUserIdAndInstitutionId(
                                reviewer.getId(),
                                course
                                        .getInstitution()
                                        .getId()
                        )
                        .orElse(null);

        if (institutionMembership != null
                && institutionMembership.getRole()
                == InstitutionRole.ADMIN) {

            return AttendanceDecisionSource.ADMIN;
        }

        throw new ForbiddenOperationException(
                "O usuário não possui permissão para revisar esta presença."
        );
    }
}