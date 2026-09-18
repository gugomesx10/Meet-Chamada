package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.Course;
import io.github.gugomesx10.meets.entity.CourseMembership;
import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.CourseRepository;
import io.github.gugomesx10.meets.repository.InstitutionMembershipRepository;
import io.github.gugomesx10.meets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseMembershipService {

    private final CourseMembershipRepository courseMembershipRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final InstitutionMembershipRepository institutionMembershipRepository;
    @Transactional
    public CourseMembership create(
            UUID courseId,
            UUID userId,
            CourseRole role
    ) {

        Course course =
                courseRepository
                        .findById(courseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Curso não encontrado."
                                )
                        );

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Usuário não encontrado."
                                )
                        );

        if (role == null) {
            throw new BusinessRuleException(
                    "O papel do usuário no curso é obrigatório."
            );
        }

        InstitutionMembership institutionMembership =
                institutionMembershipRepository
                        .findByUserIdAndInstitutionId(
                                userId,
                                course.getInstitution().getId()
                        )
                        .orElseThrow(() ->
                                new ForbiddenOperationException(
                                        "O usuário não pertence à instituição responsável pelo curso."
                                )
                        );

        validateInstitutionRole(
                institutionMembership,
                role
        );

        if (courseMembershipRepository
                .existsByUserIdAndCourseId(
                        userId,
                        courseId
                )) {

            throw new ConflictException(
                    "O usuário já pertence a este curso."
            );
        }

        CourseMembership membership =
                new CourseMembership();

        membership.setCourse(course);
        membership.setUser(user);
        membership.setRole(role);

        return courseMembershipRepository.save(
                membership
        );
    }
    @Transactional(readOnly = true)
    public CourseMembership find(
            UUID courseId,
            UUID userId
    ) {

        return courseMembershipRepository
                .findByUserIdAndCourseId(
                        userId,
                        courseId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vínculo com o curso não encontrado."
                        )
                );
    }
    @Transactional(readOnly = true)
    public List<CourseMembership> findByCourse(
            UUID courseId
    ) {

        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException(
                    "Curso não encontrado."
            );
        }

        return courseMembershipRepository
                .findAllByCourseId(courseId);
    }
    @Transactional(readOnly = true)
    public List<CourseMembership> findByUser(
            UUID userId
    ) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "Usuário não encontrado."
            );
        }

        return courseMembershipRepository
                .findAllByUserId(userId);
    }

    private void validateInstitutionRole(
            InstitutionMembership institutionMembership,
            CourseRole courseRole
    ) {

        InstitutionRole institutionRole =
                institutionMembership.getRole();

        if (courseRole == CourseRole.STUDENT
                && institutionRole != InstitutionRole.STUDENT) {

            throw new ForbiddenOperationException(
                    "Somente um aluno da instituição pode ser matriculado como aluno do curso."
            );
        }

        if (courseRole == CourseRole.INSTRUCTOR
                && institutionRole != InstitutionRole.TEACHER
                && institutionRole != InstitutionRole.ADMIN) {

            throw new ForbiddenOperationException(
                    "Somente professor ou administrador da instituição pode atuar como instrutor."
            );
        }
    }
}