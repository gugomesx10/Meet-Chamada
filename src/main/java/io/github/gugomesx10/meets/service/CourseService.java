package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.Course;
import io.github.gugomesx10.meets.entity.CourseMembership;
import io.github.gugomesx10.meets.entity.Institution;
import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.CourseStatus;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.CourseRepository;
import io.github.gugomesx10.meets.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final InstitutionRepository institutionRepository;
    private final CourseMembershipRepository courseMembershipRepository;
    private final AuthorizationService authorizationService;
    @Transactional
    public Course create(
            UUID institutionId,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            User currentUser
    ) {

        Institution institution =
                institutionRepository
                        .findById(institutionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Instituição não encontrada."
                                )
                        );

        authorizationService.requireInstitutionAdmin(
                currentUser,
                institutionId
        );

        validateName(name);
        validatePeriod(
                startDate,
                endDate
        );

        Course course = new Course();

        course.setInstitution(institution);
        course.setName(name.trim());
        course.setDescription(description);
        course.setStartDate(startDate);
        course.setEndDate(endDate);
        course.setStatus(
                CourseStatus.PLANNED
        );

        return courseRepository.save(
                course
        );
    }
    @Transactional
    public Course activate(
            UUID courseId,
            User currentUser
    ) {

        Course course =
                findRequired(courseId);

        authorizationService.requireInstitutionAdmin(
                currentUser,
                course.getInstitution().getId()
        );

        if (course.getStatus()
                != CourseStatus.PLANNED) {

            throw new ConflictException(
                    "Somente um curso planejado pode ser ativado."
            );
        }

        course.setStatus(
                CourseStatus.ACTIVE
        );

        return courseRepository.save(course);
    }
    @Transactional
    public Course complete(
            UUID courseId,
            User currentUser
    ) {

        Course course =
                findRequired(courseId);

        authorizationService.requireInstitutionAdmin(
                currentUser,
                course.getInstitution().getId()
        );

        if (course.getStatus()
                != CourseStatus.ACTIVE) {

            throw new ConflictException(
                    "Somente um curso ativo pode ser concluído."
            );
        }

        course.setStatus(
                CourseStatus.COMPLETED
        );

        return courseRepository.save(course);
    }
    @Transactional
    public Course cancel(
            UUID courseId,
            User currentUser
    ) {

        Course course =
                findRequired(courseId);

        authorizationService.requireInstitutionAdmin(
                currentUser,
                course.getInstitution().getId()
        );

        if (course.getStatus()
                == CourseStatus.COMPLETED) {

            throw new ConflictException(
                    "Um curso concluído não pode ser cancelado."
            );
        }

        if (course.getStatus()
                == CourseStatus.CANCELLED) {

            throw new ConflictException(
                    "O curso já está cancelado."
            );
        }

        course.setStatus(
                CourseStatus.CANCELLED
        );

        return courseRepository.save(course);
    }
    @Transactional(readOnly = true)
    public Course findById(
            UUID courseId,
            User currentUser
    ) {

        Course course =
                findRequired(courseId);

        authorizationService.requireCourseAccess(
                currentUser,
                course
        );

        return course;
    }
    @Transactional(readOnly = true)
    public List<Course> findByInstitution(
            UUID institutionId,
            User currentUser
    ) {

        if (!institutionRepository.existsById(
                institutionId
        )) {

            throw new ResourceNotFoundException(
                    "Instituição não encontrada."
            );
        }

        InstitutionMembership institutionMembership =
                authorizationService.requireInstitutionMember(
                        currentUser,
                        institutionId
                );

        if (institutionMembership.getRole()
                == InstitutionRole.ADMIN) {

            return courseRepository
                    .findAllByInstitutionId(
                            institutionId
                    );
        }

        return courseMembershipRepository
                .findAllByUserId(
                        currentUser.getId()
                )
                .stream()
                .map(
                        CourseMembership::getCourse
                )
                .filter(course ->
                        course.getInstitution()
                                .getId()
                                .equals(institutionId)
                )
                .toList();
    }

    private Course findRequired(
            UUID courseId
    ) {

        return courseRepository
                .findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Curso não encontrado."
                        )
                );
    }

    private void validateName(
            String name
    ) {

        if (name == null
                || name.isBlank()) {

            throw new BusinessRuleException(
                    "O nome do curso é obrigatório."
            );
        }
    }

    private void validatePeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate == null
                || endDate == null) {

            throw new BusinessRuleException(
                    "As datas de início e término são obrigatórias."
            );
        }

        if (endDate.isBefore(startDate)) {

            throw new BusinessRuleException(
                    "A data de término não pode ser anterior à data de início."
            );
        }
    }
}