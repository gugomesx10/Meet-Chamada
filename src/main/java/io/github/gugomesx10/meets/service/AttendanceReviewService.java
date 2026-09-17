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
import io.github.gugomesx10.meets.repository.AttendanceDecisionRepository;
import io.github.gugomesx10.meets.repository.AttendanceReviewRepository;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.InstitutionMembershipRepository;
import io.github.gugomesx10.meets.repository.UserRepository;
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
    private final UserRepository userRepository;

    private final CourseMembershipRepository courseMembershipRepository;
    private final InstitutionMembershipRepository institutionMembershipRepository;

    private final AuditService auditService;

    @Transactional
    public AttendanceReview review(
            UUID attendanceDecisionId,
            UUID reviewerId,
            AttendanceStatus newStatus,
            String reason
    ) {

        AttendanceDecision decision = attendanceDecisionRepository
                .findById(attendanceDecisionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Decisão de presença não encontrada."
                        )
                );

        User reviewer = userRepository
                .findById(reviewerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário responsável pela revisão não encontrado."
                        )
                );

        validateNewStatus(newStatus);

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "O motivo da revisão é obrigatório."
            );
        }

        if (decision.getStatus() == AttendanceStatus.PENDING) {
            throw new IllegalStateException(
                    "Uma presença pendente ainda não pode ser revisada."
            );
        }

        AttendanceDecisionSource source =
                validateReviewerAndResolveSource(
                        reviewerId,
                        decision
                );

        AttendanceStatus previousStatus = decision.getStatus();

        if (previousStatus == newStatus) {
            throw new IllegalStateException(
                    "O novo status deve ser diferente do status atual."
            );
        }

        Instant now = Instant.now();

        AttendanceReview review = new AttendanceReview();

        review.setAttendanceDecision(decision);
        review.setReviewer(reviewer);
        review.setPreviousStatus(previousStatus);
        review.setNewStatus(newStatus);
        review.setReason(reason);
        review.setReviewedAt(now);

        AttendanceReview savedReview =
                attendanceReviewRepository.save(review);

        decision.setStatus(newStatus);
        decision.setDecisionSource(source);
        decision.setDecidedBy(reviewer);
        decision.setDecidedAt(now);
        decision.setReason(reason);

        attendanceDecisionRepository.save(decision);

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
            UUID attendanceDecisionId
    ) {

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
                .findAllByReviewerId(reviewerId);
    }

    private void validateNewStatus(
            AttendanceStatus newStatus
    ) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "O novo status é obrigatório."
            );
        }

        if (newStatus != AttendanceStatus.CONFIRMED
                && newStatus != AttendanceStatus.ABSENT
                && newStatus != AttendanceStatus.JUSTIFIED) {

            throw new IllegalArgumentException(
                    "Uma revisão manual só pode resultar em "
                            + "CONFIRMED, ABSENT ou JUSTIFIED."
            );
        }
    }

    private AttendanceDecisionSource validateReviewerAndResolveSource(
            UUID reviewerId,
            AttendanceDecision decision
    ) {

        UUID courseId =
                decision
                        .getClassSession()
                        .getCourse()
                        .getId();

        UUID institutionId =
                decision
                        .getClassSession()
                        .getCourse()
                        .getInstitution()
                        .getId();

        CourseMembership courseMembership =
                courseMembershipRepository
                        .findByUserIdAndCourseId(
                                reviewerId,
                                courseId
                        )
                        .orElse(null);

        if (courseMembership != null
                && courseMembership.getRole() == CourseRole.INSTRUCTOR) {

            return AttendanceDecisionSource.TEACHER;
        }

        InstitutionMembership institutionMembership =
                institutionMembershipRepository
                        .findByUserIdAndInstitutionId(
                                reviewerId,
                                institutionId
                        )
                        .orElse(null);

        if (institutionMembership != null
                && institutionMembership.getRole() == InstitutionRole.ADMIN) {

            return AttendanceDecisionSource.ADMIN;
        }

        throw new IllegalStateException(
                "O usuário não possui permissão para revisar esta presença."
        );
    }
}