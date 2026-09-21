package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetConferenceRecordsResponse;
import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetParticipantSessionsResponse;
import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetParticipantsResponse;
import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetSpaceResponse;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Service
@Profile("oauth")
@RequiredArgsConstructor
public class GoogleMeetService {

    private static final Pattern MEETING_CODE_PATTERN =
            Pattern.compile("^[a-z]+-[a-z]+-[a-z]+$");

    private static final Pattern PARTICIPANT_NAME_PATTERN =
            Pattern.compile(
                    "^conferenceRecords/([^/]+)/participants/([^/]+)$"
            );

    private static final Pattern SPACE_NAME_PATTERN =
            Pattern.compile(
                    "^spaces/([^/]+)$"
            );

    private static final String CONFERENCE_RECORD_PREFIX =
            "conferenceRecords/";

    private final RestClient googleMeetRestClient;

    public GoogleMeetSpaceResponse findSpace(
            String meetingReference
    ) {

        String meetingCode =
                extractMeetingCode(
                        meetingReference
                );

        return googleMeetRestClient
                .get()
                .uri(
                        "/spaces/{meetingCode}",
                        meetingCode
                )
                .attributes(
                        clientRegistrationId(
                                "google"
                        )
                )
                .retrieve()
                .body(
                        GoogleMeetSpaceResponse.class
                );
    }

    public GoogleMeetSpaceResponse findSpaceByName(
            String spaceName
    ) {

        var matcher =
                SPACE_NAME_PATTERN
                        .matcher(
                                spaceName != null
                                        ? spaceName.trim()
                                        : ""
                        );

        if (!matcher.matches()) {

            throw new BusinessRuleException(
                    "Identificador do espaço do Google Meet inválido."
            );
        }

        String spaceId =
                matcher.group(1);

        return googleMeetRestClient
                .get()
                .uri(
                        "/spaces/{spaceId}",
                        spaceId
                )
                .attributes(
                        clientRegistrationId(
                                "google"
                        )
                )
                .retrieve()
                .body(
                        GoogleMeetSpaceResponse.class
                );
    }

    public GoogleMeetParticipantsResponse findParticipants(
            String conferenceRecord
    ) {

        String conferenceRecordId =
                extractConferenceRecordId(
                        conferenceRecord
                );

        return googleMeetRestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path(
                                                "/conferenceRecords/{conferenceRecordId}/participants"
                                        )
                                        .queryParam(
                                                "pageSize",
                                                250
                                        )
                                        .build(
                                                conferenceRecordId
                                        )
                )
                .attributes(
                        clientRegistrationId(
                                "google"
                        )
                )
                .retrieve()
                .body(
                        GoogleMeetParticipantsResponse.class
                );
    }

    public List<GoogleMeetParticipantsResponse.ParticipantResponse>
    findAllParticipants(
            String conferenceRecord
    ) {

        String conferenceRecordId =
                extractConferenceRecordId(
                        conferenceRecord
                );

        List<GoogleMeetParticipantsResponse.ParticipantResponse>
                participants =
                new ArrayList<>();

        String pageToken =
                null;

        do {

            String currentPageToken =
                    pageToken;

            GoogleMeetParticipantsResponse response =
                    googleMeetRestClient
                            .get()
                            .uri(uriBuilder -> {

                                var builder =
                                        uriBuilder
                                                .path(
                                                        "/conferenceRecords/{conferenceRecordId}/participants"
                                                )
                                                .queryParam(
                                                        "pageSize",
                                                        250
                                                );

                                if (currentPageToken != null
                                        && !currentPageToken.isBlank()) {

                                    builder.queryParam(
                                            "pageToken",
                                            currentPageToken
                                    );
                                }

                                return builder.build(
                                        conferenceRecordId
                                );
                            })
                            .attributes(
                                    clientRegistrationId(
                                            "google"
                                    )
                            )
                            .retrieve()
                            .body(
                                    GoogleMeetParticipantsResponse.class
                            );

            if (response == null) {
                break;
            }

            if (response.participants() != null) {

                participants.addAll(
                        response.participants()
                );
            }

            pageToken =
                    response.nextPageToken();

        } while (
                pageToken != null
                        && !pageToken.isBlank()
        );

        return participants;
    }

    public GoogleMeetParticipantSessionsResponse findParticipantSessions(
            String participantName
    ) {

        var matcher =
                PARTICIPANT_NAME_PATTERN
                        .matcher(
                                participantName != null
                                        ? participantName.trim()
                                        : ""
                        );

        if (!matcher.matches()) {

            throw new BusinessRuleException(
                    "Identificador do participante do Google Meet inválido."
            );
        }

        String conferenceRecordId =
                matcher.group(1);

        String participantId =
                matcher.group(2);

        return googleMeetRestClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path(
                                                "/conferenceRecords/{conferenceRecordId}/participants/{participantId}/participantSessions"
                                        )
                                        .queryParam(
                                                "pageSize",
                                                250
                                        )
                                        .build(
                                                conferenceRecordId,
                                                participantId
                                        )
                )
                .attributes(
                        clientRegistrationId(
                                "google"
                        )
                )
                .retrieve()
                .body(
                        GoogleMeetParticipantSessionsResponse.class
                );
    }

    public List<GoogleMeetParticipantSessionsResponse.ParticipantSessionResponse>
    findAllParticipantSessions(
            String participantName
    ) {

        var matcher =
                PARTICIPANT_NAME_PATTERN
                        .matcher(
                                participantName != null
                                        ? participantName.trim()
                                        : ""
                        );

        if (!matcher.matches()) {

            throw new BusinessRuleException(
                    "Identificador do participante do Google Meet inválido."
            );
        }

        String conferenceRecordId =
                matcher.group(1);

        String participantId =
                matcher.group(2);

        List<GoogleMeetParticipantSessionsResponse.ParticipantSessionResponse>
                sessions =
                new ArrayList<>();

        String pageToken =
                null;

        do {

            String currentPageToken =
                    pageToken;

            GoogleMeetParticipantSessionsResponse response =
                    googleMeetRestClient
                            .get()
                            .uri(uriBuilder -> {

                                var builder =
                                        uriBuilder
                                                .path(
                                                        "/conferenceRecords/{conferenceRecordId}/participants/{participantId}/participantSessions"
                                                )
                                                .queryParam(
                                                        "pageSize",
                                                        250
                                                );

                                if (currentPageToken != null
                                        && !currentPageToken.isBlank()) {

                                    builder.queryParam(
                                            "pageToken",
                                            currentPageToken
                                    );
                                }

                                return builder.build(
                                        conferenceRecordId,
                                        participantId
                                );
                            })
                            .attributes(
                                    clientRegistrationId(
                                            "google"
                                    )
                            )
                            .retrieve()
                            .body(
                                    GoogleMeetParticipantSessionsResponse.class
                            );

            if (response == null) {
                break;
            }

            if (response.participantSessions()
                    != null) {

                sessions.addAll(
                        response.participantSessions()
                );
            }

            pageToken =
                    response.nextPageToken();

        } while (
                pageToken != null
                        && !pageToken.isBlank()
        );

        return sessions;
    }

    public List<GoogleMeetConferenceRecordsResponse.ConferenceRecordResponse>
    findConferenceRecordsBySpaceName(
            String spaceName
    ) {

        String normalizedSpaceName =
                normalizeSpaceName(
                        spaceName
                );

        List<GoogleMeetConferenceRecordsResponse.ConferenceRecordResponse>
                conferenceRecords =
                new ArrayList<>();

        String pageToken =
                null;

        do {

            String currentPageToken =
                    pageToken;

            GoogleMeetConferenceRecordsResponse response =
                    googleMeetRestClient
                            .get()
                            .uri(uriBuilder -> {

                                var builder =
                                        uriBuilder
                                                .path(
                                                        "/conferenceRecords"
                                                )
                                                .queryParam(
                                                        "pageSize",
                                                        100
                                                )
                                                .queryParam(
                                                        "filter",
                                                        "space.name = \""
                                                                + normalizedSpaceName
                                                                + "\""
                                                );

                                if (currentPageToken != null
                                        && !currentPageToken.isBlank()) {

                                    builder.queryParam(
                                            "pageToken",
                                            currentPageToken
                                    );
                                }

                                return builder.build();
                            })
                            .attributes(
                                    clientRegistrationId(
                                            "google"
                                    )
                            )
                            .retrieve()
                            .body(
                                    GoogleMeetConferenceRecordsResponse.class
                            );

            if (response == null) {
                break;
            }

            if (response.conferenceRecords()
                    != null) {

                conferenceRecords.addAll(
                        response.conferenceRecords()
                );
            }

            pageToken =
                    response.nextPageToken();

        } while (
                pageToken != null
                        && !pageToken.isBlank()
        );

        return conferenceRecords;
    }

    private String extractMeetingCode(
            String meetingReference
    ) {

        if (meetingReference == null
                || meetingReference.isBlank()) {

            throw new BusinessRuleException(
                    "Referência do Google Meet é obrigatória."
            );
        }

        String value =
                meetingReference
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (value.startsWith(
                "http://"
        ) || value.startsWith(
                "https://"
        )) {

            try {

                URI uri =
                        URI.create(
                                value
                        );

                if (uri.getHost() == null
                        || !"meet.google.com"
                        .equalsIgnoreCase(
                                uri.getHost()
                        )) {

                    throw new BusinessRuleException(
                            "A URL informada não pertence ao Google Meet."
                    );
                }

                value =
                        uri.getPath();

            } catch (
                    IllegalArgumentException exception
            ) {

                throw new BusinessRuleException(
                        "URL do Google Meet inválida."
                );
            }
        }

        value =
                value.replaceAll(
                        "^/+|/+$",
                        ""
                );

        if (!MEETING_CODE_PATTERN
                .matcher(value)
                .matches()) {

            throw new BusinessRuleException(
                    "Código do Google Meet inválido."
            );
        }

        return value;
    }

    private String extractConferenceRecordId(
            String conferenceRecord
    ) {

        if (conferenceRecord == null
                || conferenceRecord.isBlank()) {

            throw new BusinessRuleException(
                    "Registro de conferência do Google Meet é obrigatório."
            );
        }

        String value =
                conferenceRecord.trim();

        if (!value.startsWith(
                CONFERENCE_RECORD_PREFIX
        )) {

            throw new BusinessRuleException(
                    "Registro de conferência do Google Meet inválido."
            );
        }

        String conferenceRecordId =
                value.substring(
                        CONFERENCE_RECORD_PREFIX.length()
                );

        if (conferenceRecordId.isBlank()
                || conferenceRecordId.contains(
                "/"
        )) {

            throw new BusinessRuleException(
                    "Registro de conferência do Google Meet inválido."
            );
        }

        return conferenceRecordId;
    }

    private String normalizeSpaceName(
            String spaceName
    ) {

        if (spaceName == null
                || spaceName.isBlank()) {

            throw new BusinessRuleException(
                    "Identificador do espaço do Google Meet é obrigatório."
            );
        }

        String value =
                spaceName.trim();

        if (!SPACE_NAME_PATTERN
                .matcher(value)
                .matches()) {

            throw new BusinessRuleException(
                    "Identificador do espaço do Google Meet inválido."
            );
        }

        return value;
    }
}