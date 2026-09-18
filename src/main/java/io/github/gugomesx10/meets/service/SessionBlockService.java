package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.CourseMembership;
import io.github.gugomesx10.meets.entity.SessionBlock;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.ClassSessionStatus;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import io.github.gugomesx10.meets.entity.enums.SessionBlockType;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.ClassSessionRepository;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.SessionBlockRepository;
import io.github.gugomesx10.meets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionBlockService {
    private final SessionBlockRepository sessionBlockRepository;
    private final ClassSessionRepository classSessionRepository;
    private final UserRepository userRepository;
    private final CourseMembershipRepository courseMembershipRepository;
    @Transactional
    public SessionBlock create(
            UUID classSessionId,
            UUID instructorId,
            String title,
            String description,
            SessionBlockType type,
            LocalTime startTime,
            LocalTime endTime
    ) {

        ClassSession classSession =
                classSessionRepository
                        .findById(classSessionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sessão de aula não encontrada."
                                )
                        );

        User instructor =
                userRepository
                        .findById(instructorId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Instrutor não encontrado."
                                )
                        );

        validateSession(classSession);

        validateInstructor(
                instructor.getId(),
                classSession
        );

        validateTitle(title);
        validateType(type);

        validatePeriod(
                classSession,
                startTime,
                endTime
        );

        validateOverlap(
                classSession.getId(),
                startTime,
                endTime
        );

        SessionBlock block =
                new SessionBlock();

        block.setClassSession(classSession);
        block.setInstructor(instructor);
        block.setTitle(title.trim());
        block.setDescription(description);
        block.setType(type);
        block.setStartTime(startTime);
        block.setEndTime(endTime);

        return sessionBlockRepository.save(block);
    }
    @Transactional(readOnly = true)
    public SessionBlock findById(
            UUID sessionBlockId
    ) {

        return findRequired(sessionBlockId);
    }
    @Transactional(readOnly = true)
    public List<SessionBlock> findBySession(
            UUID classSessionId
    ) {

        if (!classSessionRepository.existsById(classSessionId)) {
            throw new ResourceNotFoundException(
                    "Sessão de aula não encontrada."
            );
        }

        return sessionBlockRepository
                .findAllByClassSessionIdOrderByStartTimeAsc(
                        classSessionId
                );
    }
    @Transactional(readOnly = true)
    public List<SessionBlock> findByInstructor(
            UUID instructorId
    ) {

        if (!userRepository.existsById(instructorId)) {
            throw new ResourceNotFoundException(
                    "Instrutor não encontrado."
            );
        }

        return sessionBlockRepository
                .findAllByInstructorId(
                        instructorId
                );
    }

    private SessionBlock findRequired(
            UUID sessionBlockId
    ) {

        return sessionBlockRepository
                .findById(sessionBlockId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bloco da aula não encontrado."
                        )
                );
    }

    private void validateSession(
            ClassSession classSession
    ) {

        if (classSession.getStatus()
                == ClassSessionStatus.COMPLETED) {

            throw new ConflictException(
                    "Não é possível adicionar blocos a uma aula concluída."
            );
        }

        if (classSession.getStatus()
                == ClassSessionStatus.CANCELLED) {

            throw new ConflictException(
                    "Não é possível adicionar blocos a uma aula cancelada."
            );
        }
    }

    private void validateInstructor(
            UUID instructorId,
            ClassSession classSession
    ) {

        CourseMembership membership =
                courseMembershipRepository
                        .findByUserIdAndCourseId(
                                instructorId,
                                classSession.getCourse().getId()
                        )
                        .orElseThrow(() ->
                                new ForbiddenOperationException(
                                        "O usuário não pertence ao curso."
                                )
                        );

        if (membership.getRole()
                != CourseRole.INSTRUCTOR) {

            throw new ForbiddenOperationException(
                    "O usuário informado não é instrutor deste curso."
            );
        }
    }

    private void validateTitle(
            String title
    ) {

        if (title == null || title.isBlank()) {
            throw new BusinessRuleException(
                    "O título do bloco é obrigatório."
            );
        }
    }

    private void validateType(
            SessionBlockType type
    ) {

        if (type == null) {
            throw new BusinessRuleException(
                    "O tipo do bloco é obrigatório."
            );
        }
    }

    private void validatePeriod(
            ClassSession classSession,
            LocalTime startTime,
            LocalTime endTime
    ) {

        if (startTime == null || endTime == null) {
            throw new BusinessRuleException(
                    "Os horários do bloco são obrigatórios."
            );
        }

        if (!startTime.isBefore(endTime)) {
            throw new BusinessRuleException(
                    "O horário inicial do bloco deve ser anterior ao horário final."
            );
        }

        if (startTime.isBefore(
                classSession.getStartTime()
        )) {

            throw new BusinessRuleException(
                    "O bloco não pode começar antes da aula."
            );
        }

        if (endTime.isAfter(
                classSession.getEndTime()
        )) {

            throw new BusinessRuleException(
                    "O bloco não pode terminar após o fim da aula."
            );
        }
    }

    private void validateOverlap(
            UUID classSessionId,
            LocalTime startTime,
            LocalTime endTime
    ) {

        List<SessionBlock> existingBlocks =
                sessionBlockRepository
                        .findAllByClassSessionIdOrderByStartTimeAsc(
                                classSessionId
                        );

        boolean overlaps =
                existingBlocks.stream()
                        .anyMatch(existing ->
                                startTime.isBefore(
                                        existing.getEndTime()
                                )
                                        &&
                                        endTime.isAfter(
                                                existing.getStartTime()
                                        )
                        );

        if (overlaps) {
            throw new ConflictException(
                    "O horário informado se sobrepõe a outro bloco da aula."
            );
        }
    }
}