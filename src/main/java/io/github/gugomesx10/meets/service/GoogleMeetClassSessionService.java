package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetSpaceResponse;
import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.ClassSessionStatus;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.ClassSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Profile("oauth")
@RequiredArgsConstructor
public class GoogleMeetClassSessionService {

    private final ClassSessionRepository classSessionRepository;
    private final GoogleMeetService googleMeetService;
    private final AuthorizationService authorizationService;
    @Transactional
    public ClassSession link(
            UUID classSessionId,
            String meetingReference,
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

        if (classSession.getStatus()
                == ClassSessionStatus.CANCELLED) {

            throw new ConflictException(
                    "Uma aula cancelada não pode ser vinculada ao Google Meet."
            );
        }

        GoogleMeetSpaceResponse space =
                googleMeetService.findSpace(
                        meetingReference
                );

        String currentSpaceName =
                classSession.getGoogleMeetSpaceName();

        if (currentSpaceName != null
                && !currentSpaceName.equals(
                space.name()
        )) {

            throw new ConflictException(
                    "A aula já está vinculada a outro espaço do Google Meet."
            );
        }

        classSession.setGoogleMeetSpaceName(
                space.name()
        );

        if (space.activeConference() != null
                && space.activeConference()
                .conferenceRecord() != null
                && !space.activeConference()
                .conferenceRecord()
                .isBlank()) {

            classSession
                    .setGoogleMeetConferenceRecordName(
                            space.activeConference()
                                    .conferenceRecord()
                    );
        }

        return classSessionRepository.save(
                classSession
        );
    }
}