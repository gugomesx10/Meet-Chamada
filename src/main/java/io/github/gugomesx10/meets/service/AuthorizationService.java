package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.Course;
import io.github.gugomesx10.meets.entity.CourseMembership;
import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.InstitutionMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final InstitutionMembershipRepository institutionMembershipRepository;
    private final CourseMembershipRepository courseMembershipRepository;

    public InstitutionMembership requireInstitutionMember(
            User user,
            UUID institutionId
    ) {

        return institutionMembershipRepository
                .findByUserIdAndInstitutionId(
                        user.getId(),
                        institutionId
                )
                .orElseThrow(() ->
                        new ForbiddenOperationException(
                                "O usuário não pertence a esta instituição."
                        )
                );
    }

    public InstitutionMembership requireInstitutionAdmin(
            User user,
            UUID institutionId
    ) {

        InstitutionMembership membership =
                requireInstitutionMember(
                        user,
                        institutionId
                );

        if (membership.getRole() != InstitutionRole.ADMIN) {

            throw new ForbiddenOperationException(
                    "Somente um administrador da instituição pode executar esta operação."
            );
        }

        return membership;
    }

    public void requireInstitutionAdminOrSelf(
            User user,
            UUID institutionId,
            UUID targetUserId
    ) {

        if (user.getId().equals(targetUserId)) {

            requireInstitutionMember(
                    user,
                    institutionId
            );

            return;
        }

        requireInstitutionAdmin(
                user,
                institutionId
        );
    }

    public void requireCourseAccess(
            User user,
            Course course
    ) {

        InstitutionMembership institutionMembership =
                requireInstitutionMember(
                        user,
                        course.getInstitution().getId()
                );

        if (institutionMembership.getRole()
                == InstitutionRole.ADMIN) {

            return;
        }

        courseMembershipRepository
                .findByUserIdAndCourseId(
                        user.getId(),
                        course.getId()
                )
                .orElseThrow(() ->
                        new ForbiddenOperationException(
                                "O usuário não pertence a este curso."
                        )
                );
    }

    public void requireCourseInstructorOrAdmin(
            User user,
            Course course
    ) {

        InstitutionMembership institutionMembership =
                requireInstitutionMember(
                        user,
                        course.getInstitution().getId()
                );

        if (institutionMembership.getRole()
                == InstitutionRole.ADMIN) {

            return;
        }

        CourseMembership courseMembership =
                courseMembershipRepository
                        .findByUserIdAndCourseId(
                                user.getId(),
                                course.getId()
                        )
                        .orElseThrow(() ->
                                new ForbiddenOperationException(
                                        "O usuário não pertence a este curso."
                                )
                        );

        if (courseMembership.getRole()
                != CourseRole.INSTRUCTOR) {

            throw new ForbiddenOperationException(
                    "Somente um instrutor do curso ou administrador da instituição pode executar esta operação."
            );
        }
    }

    public void requireCourseInstructorOrAdminOrSelf(
            User user,
            Course course,
            UUID targetUserId
    ) {

        if (user.getId().equals(targetUserId)) {

            requireCourseAccess(
                    user,
                    course
            );

            return;
        }

        requireCourseInstructorOrAdmin(
                user,
                course
        );
    }
}