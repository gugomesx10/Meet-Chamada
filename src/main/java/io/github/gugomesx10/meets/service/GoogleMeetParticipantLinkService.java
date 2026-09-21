package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.CourseMembership;
import io.github.gugomesx10.meets.entity.GoogleMeetParticipantLink;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.ClassSessionRepository;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.GoogleMeetParticipantLinkRepository;
import io.github.gugomesx10.meets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class GoogleMeetParticipantLinkService {

    private static final Pattern GOOGLE_MEET_USER_PATTERN =
            Pattern.compile(
                    "^users/[^/]+$"
            );

    private final GoogleMeetParticipantLinkRepository googleMeetParticipantLinkRepository;
    private final ClassSessionRepository classSessionRepository;
    private final CourseMembershipRepository courseMembershipRepository;
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    @Transactional
    public GoogleMeetParticipantLink link(
            UUID classSessionId,
            UUID studentId,
            String googleMeetUserName,
            User currentUser
    ) {

        ClassSession classSession =
                classSessionRepository
                        .findById(classSessionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sessão de aula não encontrada."
                                )
                        );

        authorizationService
                .requireCourseInstructorOrAdmin(
                        currentUser,
                        classSession.getCourse()
                );

        User student =
                userRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Aluno não encontrado."
                                )
                        );

        validateStudent(
                studentId,
                classSession
        );

        String normalizedGoogleMeetUserName =
                normalizeGoogleMeetUserName(
                        googleMeetUserName
                );

        var existing =
                googleMeetParticipantLinkRepository
                        .findByGoogleMeetUserName(
                                normalizedGoogleMeetUserName
                        );

        if (existing.isPresent()) {

            GoogleMeetParticipantLink currentLink =
                    existing.get();

            if (currentLink
                    .getUser()
                    .getId()
                    .equals(studentId)) {

                return currentLink;
            }

            throw new ConflictException(
                    "Este participante do Google Meet já está vinculado a outro usuário."
            );
        }

        GoogleMeetParticipantLink link =
                new GoogleMeetParticipantLink();

        link.setUser(
                student
        );

        link.setGoogleMeetUserName(
                normalizedGoogleMeetUserName
        );

        GoogleMeetParticipantLink saved =
                googleMeetParticipantLinkRepository.save(
                        link
                );

        auditService.register(
                currentUser,
                "GOOGLE_MEET_PARTICIPANT_LINKED",
                "GoogleMeetParticipantLink",
                saved.getId(),
                "Participante do Google Meet vinculado ao aluno "
                        + student.getId()
                        + "."
        );

        return saved;
    }
    @Transactional(readOnly = true)
    public GoogleMeetParticipantLink findByGoogleMeetUserName(
            String googleMeetUserName
    ) {

        String normalizedGoogleMeetUserName =
                normalizeGoogleMeetUserName(
                        googleMeetUserName
                );

        return googleMeetParticipantLinkRepository
                .findByGoogleMeetUserName(
                        normalizedGoogleMeetUserName
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Participante do Google Meet não vinculado."
                        )
                );
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
                                new BusinessRuleException(
                                        "O usuário não pertence ao curso desta aula."
                                )
                        );

        if (membership.getRole()
                != CourseRole.STUDENT) {

            throw new BusinessRuleException(
                    "O usuário informado não é aluno deste curso."
            );
        }
    }

    private String normalizeGoogleMeetUserName(
            String googleMeetUserName
    ) {

        if (googleMeetUserName == null
                || googleMeetUserName.isBlank()) {

            throw new BusinessRuleException(
                    "O identificador do participante do Google Meet é obrigatório."
            );
        }

        String value =
                googleMeetUserName.trim();

        if (!GOOGLE_MEET_USER_PATTERN
                .matcher(value)
                .matches()) {

            throw new BusinessRuleException(
                    "O identificador do participante do Google Meet é inválido."
            );
        }

        return value;
    }
}