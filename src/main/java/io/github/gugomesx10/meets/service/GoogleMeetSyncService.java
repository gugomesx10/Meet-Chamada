package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetConferenceRecordsResponse;
import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetSyncResponse;
import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.GoogleMeetParticipantLink;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import io.github.gugomesx10.meets.entity.enums.EvidenceSource;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.ClassSessionRepository;
import io.github.gugomesx10.meets.repository.CourseMembershipRepository;
import io.github.gugomesx10.meets.repository.GoogleMeetParticipantLinkRepository;
import io.github.gugomesx10.meets.repository.PresenceEvidenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Profile("oauth")
@RequiredArgsConstructor
public class GoogleMeetSyncService {
    private final ClassSessionRepository classSessionRepository;
    private final GoogleMeetService googleMeetService;
    private final GoogleMeetParticipantLinkRepository googleMeetParticipantLinkRepository;
    private final CourseMembershipRepository courseMembershipRepository;
    private final PresenceEvidenceRepository presenceEvidenceRepository;
    private final PresenceEvidenceService presenceEvidenceService;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    @Transactional
    public GoogleMeetSyncResponse sync(
            UUID classSessionId,
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

        if (classSession.getGoogleMeetSpaceName() == null
                || classSession.getGoogleMeetSpaceName().isBlank()) {

            throw new BusinessRuleException(
                    "A aula ainda não possui um espaço do Google Meet vinculado."
            );
        }

        String conferenceRecordName =
                resolveConferenceRecord(
                        classSession
                );

        var participants =
                googleMeetService
                        .findAllParticipants(
                                conferenceRecordName
                        );

        int mappedParticipants = 0;
        int unmappedParticipants = 0;
        int unsupportedParticipants = 0;
        int linkedUsersOutsideCourse = 0;
        int sessionsImported = 0;
        int sessionsAlreadyImported = 0;

        for (var participant : participants) {

            if (participant.signedinUser() == null
                    || participant.signedinUser().user() == null
                    || participant.signedinUser().user().isBlank()) {

                unsupportedParticipants++;
                continue;
            }

            String googleMeetUserName =
                    participant
                            .signedinUser()
                            .user();

            var optionalLink =
                    googleMeetParticipantLinkRepository
                            .findByGoogleMeetUserName(
                                    googleMeetUserName
                            );

            if (optionalLink.isEmpty()) {

                unmappedParticipants++;
                continue;
            }

            GoogleMeetParticipantLink link =
                    optionalLink.get();

            User student =
                    link.getUser();

            var membership =
                    courseMembershipRepository
                            .findByUserIdAndCourseId(
                                    student.getId(),
                                    classSession
                                            .getCourse()
                                            .getId()
                            );

            if (membership.isEmpty()
                    || membership.get().getRole()
                    != CourseRole.STUDENT) {

                linkedUsersOutsideCourse++;
                continue;
            }

            mappedParticipants++;

            var participantSessions =
                    googleMeetService
                            .findAllParticipantSessions(
                                    participant.name()
                            );

            for (var participantSession : participantSessions) {

                if (participantSession.name() == null
                        || participantSession.name().isBlank()
                        || participantSession.startTime() == null) {

                    continue;
                }

                boolean alreadyImported =
                        presenceEvidenceRepository
                                .existsBySourceAndExternalReference(
                                        EvidenceSource.GOOGLE_MEET,
                                        participantSession.name()
                                );

                if (alreadyImported) {

                    sessionsAlreadyImported++;
                    continue;
                }

                presenceEvidenceService
                        .registerMeetingSession(
                                student,
                                classSession,
                                participantSession.startTime(),
                                participantSession.endTime(),
                                participantSession.name()
                        );

                sessionsImported++;
            }
        }

        auditService.register(
                currentUser,
                "GOOGLE_MEET_SYNC_COMPLETED",
                "ClassSession",
                classSession.getId(),
                "Sincronização do Google Meet concluída. "
                        + "Participantes encontrados: "
                        + participants.size()
                        + ", mapeados: "
                        + mappedParticipants
                        + ", não mapeados: "
                        + unmappedParticipants
                        + ", não suportados: "
                        + unsupportedParticipants
                        + ", fora do curso: "
                        + linkedUsersOutsideCourse
                        + ", sessões importadas: "
                        + sessionsImported
                        + ", sessões já importadas: "
                        + sessionsAlreadyImported
                        + "."
        );

        return new GoogleMeetSyncResponse(
                classSession.getId(),
                conferenceRecordName,
                participants.size(),
                mappedParticipants,
                unmappedParticipants,
                unsupportedParticipants,
                linkedUsersOutsideCourse,
                sessionsImported,
                sessionsAlreadyImported
        );
    }

    private String resolveConferenceRecord(
            ClassSession classSession
    ) {

        String currentConferenceRecord =
                classSession
                        .getGoogleMeetConferenceRecordName();

        if (currentConferenceRecord != null
                && !currentConferenceRecord.isBlank()) {

            return currentConferenceRecord;
        }

        var space =
                googleMeetService
                        .findSpaceByName(
                                classSession
                                        .getGoogleMeetSpaceName()
                        );

        if (space != null
                && space.activeConference() != null
                && space.activeConference()
                .conferenceRecord() != null
                && !space.activeConference()
                .conferenceRecord()
                .isBlank()) {

            String conferenceRecord =
                    space.activeConference()
                            .conferenceRecord();

            classSession
                    .setGoogleMeetConferenceRecordName(
                            conferenceRecord
                    );

            classSessionRepository.save(
                    classSession
            );

            return conferenceRecord;
        }

        List<GoogleMeetConferenceRecordsResponse.ConferenceRecordResponse>
                conferenceRecords =
                googleMeetService
                        .findConferenceRecordsBySpaceName(
                                classSession
                                        .getGoogleMeetSpaceName()
                        );

        if (conferenceRecords.isEmpty()) {

            throw new ResourceNotFoundException(
                    "Nenhum registro de conferência foi encontrado para este espaço do Google Meet."
            );
        }

        if (conferenceRecords.size() > 1) {

            throw new ConflictException(
                    "Mais de uma conferência foi encontrada para este espaço do Google Meet. Não é possível determinar automaticamente qual pertence à aula."
            );
        }

        String conferenceRecord =
                conferenceRecords
                        .getFirst()
                        .name();

        if (conferenceRecord == null
                || conferenceRecord.isBlank()) {

            throw new BusinessRuleException(
                    "O registro de conferência retornado pelo Google Meet é inválido."
            );
        }

        classSession
                .setGoogleMeetConferenceRecordName(
                        conferenceRecord
                );

        classSessionRepository.save(
                classSession
        );

        return conferenceRecord;
    }
}