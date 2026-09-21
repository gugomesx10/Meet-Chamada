package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.Course;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.ClassSessionStatus;
import io.github.gugomesx10.meets.entity.enums.CourseStatus;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.ClassSessionRepository;
import io.github.gugomesx10.meets.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassSessionService {
    private final ClassSessionRepository classSessionRepository;
    private final CourseRepository courseRepository;
    private final AuthorizationService authorizationService;
    @Transactional
    public ClassSession create(
            UUID courseId,
            String title,
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            User currentUser
    ) {

        Course course =
                courseRepository
                        .findById(courseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Curso não encontrado."
                                )
                        );

        authorizationService.requireCourseInstructorOrAdmin(
                currentUser,
                course
        );

        validateCourse(course);
        validateTitle(title);

        validatePeriod(
                sessionDate,
                startTime,
                endTime,
                course
        );

        ClassSession session =
                new ClassSession();

        session.setCourse(course);
        session.setTitle(title.trim());
        session.setSessionDate(sessionDate);
        session.setStartTime(startTime);
        session.setEndTime(endTime);
        session.setStatus(
                ClassSessionStatus.SCHEDULED
        );

        return classSessionRepository.save(
                session
        );
    }
    @Transactional
    public ClassSession start(
            UUID classSessionId,
            User currentUser
    ) {

        ClassSession session =
                findRequired(classSessionId);

        authorizationService.requireCourseInstructorOrAdmin(
                currentUser,
                session.getCourse()
        );

        if (session.getStatus()
                != ClassSessionStatus.SCHEDULED) {

            throw new ConflictException(
                    "Somente uma aula agendada pode ser iniciada."
            );
        }

        session.setStatus(
                ClassSessionStatus.IN_PROGRESS
        );

        return classSessionRepository.save(
                session
        );
    }
    @Transactional
    public ClassSession complete(
            UUID classSessionId,
            User currentUser
    ) {

        ClassSession session =
                findRequired(classSessionId);

        authorizationService.requireCourseInstructorOrAdmin(
                currentUser,
                session.getCourse()
        );

        if (session.getStatus()
                != ClassSessionStatus.IN_PROGRESS) {

            throw new ConflictException(
                    "Somente uma aula em andamento pode ser concluída."
            );
        }

        session.setStatus(
                ClassSessionStatus.COMPLETED
        );

        return classSessionRepository.save(
                session
        );
    }
    @Transactional
    public ClassSession cancel(
            UUID classSessionId,
            User currentUser
    ) {

        ClassSession session =
                findRequired(classSessionId);

        authorizationService.requireCourseInstructorOrAdmin(
                currentUser,
                session.getCourse()
        );

        if (session.getStatus()
                == ClassSessionStatus.COMPLETED) {

            throw new ConflictException(
                    "Uma aula concluída não pode ser cancelada."
            );
        }

        if (session.getStatus()
                == ClassSessionStatus.CANCELLED) {

            throw new ConflictException(
                    "A aula já está cancelada."
            );
        }

        session.setStatus(
                ClassSessionStatus.CANCELLED
        );

        return classSessionRepository.save(
                session
        );
    }
    @Transactional(readOnly = true)
    public ClassSession findById(
            UUID classSessionId,
            User currentUser
    ) {

        ClassSession session =
                findRequired(classSessionId);

        authorizationService.requireCourseAccess(
                currentUser,
                session.getCourse()
        );

        return session;
    }
    @Transactional(readOnly = true)
    public List<ClassSession> findByCourse(
            UUID courseId,
            User currentUser
    ) {

        Course course =
                courseRepository
                        .findById(courseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Curso não encontrado."
                                )
                        );

        authorizationService.requireCourseAccess(
                currentUser,
                course
        );

        return classSessionRepository
                .findAllByCourseIdOrderBySessionDateAsc(
                        courseId
                );
    }

    private ClassSession findRequired(
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

    private void validateTitle(
            String title
    ) {

        if (title == null || title.isBlank()) {

            throw new BusinessRuleException(
                    "O título da aula é obrigatório."
            );
        }
    }

    private void validateCourse(
            Course course
    ) {

        if (course.getStatus()
                == CourseStatus.CANCELLED) {

            throw new ConflictException(
                    "Não é possível criar uma aula em um curso cancelado."
            );
        }

        if (course.getStatus()
                == CourseStatus.COMPLETED) {

            throw new ConflictException(
                    "Não é possível criar uma aula em um curso concluído."
            );
        }
    }

    private void validatePeriod(
            LocalDate sessionDate,
            LocalTime startTime,
            LocalTime endTime,
            Course course
    ) {

        if (sessionDate == null
                || startTime == null
                || endTime == null) {

            throw new BusinessRuleException(
                    "Data e horários da aula são obrigatórios."
            );
        }

        if (!startTime.isBefore(endTime)) {

            throw new BusinessRuleException(
                    "O horário inicial deve ser anterior ao horário final."
            );
        }

        if (course.getStartDate() != null
                && sessionDate.isBefore(
                course.getStartDate()
        )) {

            throw new BusinessRuleException(
                    "A aula não pode ocorrer antes do início do curso."
            );
        }

        if (course.getEndDate() != null
                && sessionDate.isAfter(
                course.getEndDate()
        )) {

            throw new BusinessRuleException(
                    "A aula não pode ocorrer após o término do curso."
            );
        }
    }
}