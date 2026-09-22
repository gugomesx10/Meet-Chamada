package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetSyncResponse;
import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.GoogleMeetParticipantLink;
import io.github.gugomesx10.meets.entity.PresenceEvidence;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@Profile("oauth")
@RequiredArgsConstructor
public class GoogleMeetSyncService {

    private static final ZoneId APPLICATION_ZONE =
            ZoneId.of("America/Sao_Paulo");

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
                || classSession
                .getGoogleMeetSpaceName()
                .isBlank()) {

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

                var existingEvidence =
                        presenceEvidenceRepository
                                .findBySourceAndExternalReference(
                                        EvidenceSource.GOOGLE_MEET,
                                        participantSession.name()
                                );

                if (existingEvidence.isPresent()) {

                    PresenceEvidence evidence =
                            existingEvidence.get();

                    /*
                     * Uma referência externa nunca pode mudar
                     * silenciosamente de aluno ou de aula.
                     */
                    if (!evidence.getStudent()
                            .getId()
                            .equals(student.getId())
                            || !evidence.getClassSession()
                            .getId()
                            .equals(classSession.getId())) {

                        throw new ConflictException(
                                "A sessão externa do Google Meet já está vinculada a outra evidência."
                        );
                    }

                    /*
                     * Durante a aula:
                     * endTime = null
                     *
                     * Depois que o participante sai:
                     * endTime passa a existir.
                     */
                    if (evidence.getEndedAt() == null
                            && participantSession.endTime() != null) {

                        evidence.setEndedAt(
                                participantSession.endTime()
                        );

                        presenceEvidenceRepository.save(
                                evidence
                        );
                    }

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

        String storedConferenceRecord =
                classSession
                        .getGoogleMeetConferenceRecordName();

        /*
         * Se já capturamos a conferência desta aula,
         * ela é a fonte da verdade.
         */
        if (storedConferenceRecord != null
                && !storedConferenceRecord.isBlank()) {

            return storedConferenceRecord;
        }

        /*
         * Uma aula antiga sem conferenceRecord não pode
         * simplesmente pegar a conferência ativa de hoje.
         */
        if (!isToday(classSession)) {

            throw new BusinessRuleException(
                    "A aula não possui um registro de conferência salvo. "
                            + "Não é seguro associar automaticamente uma conferência atual a uma aula histórica."
            );
        }

        var space =
                googleMeetService
                        .findSpaceByName(
                                classSession
                                        .getGoogleMeetSpaceName()
                        );

        if (space == null
                || space.activeConference() == null
                || space.activeConference()
                .conferenceRecord() == null
                || space.activeConference()
                .conferenceRecord()
                .isBlank()) {

            throw new BusinessRuleException(
                    "Nenhuma conferência ativa foi encontrada para a aula de hoje."
            );
        }

        String conferenceRecord =
                space.activeConference()
                        .conferenceRecord();

        /*
         * Captura uma única vez.
         */
        classSession
                .setGoogleMeetConferenceRecordName(
                        conferenceRecord
                );

        classSessionRepository.save(
                classSession
        );

        return conferenceRecord;
    }

    private boolean isToday(
            ClassSession classSession
    ) {

        return LocalDate.now(
                APPLICATION_ZONE
        ).equals(
                classSession.getSessionDate()
        );
    }
}