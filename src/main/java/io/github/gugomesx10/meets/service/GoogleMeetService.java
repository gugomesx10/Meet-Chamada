package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetParticipantSessionsResponse;
import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetParticipantsResponse;
import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetSpaceResponse;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Service
@Profile("oauth")
@RequiredArgsConstructor
public class GoogleMeetService {

    private static final Pattern MEETING_CODE_PATTERN =
            Pattern.compile(
                    "^[a-z]+-[a-z]+-[a-z]+$"
            );

    private static final Pattern PARTICIPANT_NAME_PATTERN =
            Pattern.compile(
                    "^conferenceRecords/([^/]+)/participants/([^/]+)$"
            );

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

    public GoogleMeetParticipantsResponse findParticipants(
            String conferenceRecord
    ) {

        if (conferenceRecord == null
                || conferenceRecord.isBlank()) {

            throw new BusinessRuleException(
                    "O registro da conferência é obrigatório."
            );
        }

        String prefix =
                "conferenceRecords/";

        if (!conferenceRecord.startsWith(prefix)) {

            throw new BusinessRuleException(
                    "O registro da conferência é inválido."
            );
        }

        String conferenceRecordId =
                conferenceRecord.substring(
                        prefix.length()
                );

        if (conferenceRecordId.isBlank()
                || conferenceRecordId.contains("/")) {

            throw new BusinessRuleException(
                    "O registro da conferência é inválido."
            );
        }

        return googleMeetRestClient
                .get()
                .uri(
                        "/conferenceRecords/{conferenceRecordId}/participants?pageSize=250",
                        conferenceRecordId
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

    public GoogleMeetParticipantSessionsResponse findParticipantSessions(
            String participantName
    ) {

        if (participantName == null
                || participantName.isBlank()) {

            throw new BusinessRuleException(
                    "O participante é obrigatório."
            );
        }

        var matcher =
                PARTICIPANT_NAME_PATTERN.matcher(
                        participantName.trim()
                );

        if (!matcher.matches()) {

            throw new BusinessRuleException(
                    "O identificador do participante é inválido."
            );
        }

        String conferenceRecordId =
                matcher.group(1);

        String participantId =
                matcher.group(2);

        return googleMeetRestClient
                .get()
                .uri(
                        "/conferenceRecords/{conferenceRecordId}"
                                + "/participants/{participantId}"
                                + "/participantSessions?pageSize=250",
                        conferenceRecordId,
                        participantId
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

    private String extractMeetingCode(
            String meetingReference
    ) {

        if (meetingReference == null
                || meetingReference.isBlank()) {

            throw new BusinessRuleException(
                    "O link ou código do Google Meet é obrigatório."
            );
        }

        String value =
                meetingReference.trim();

        if (value.startsWith("https://")
                || value.startsWith("http://")) {

            try {

                URI uri =
                        URI.create(
                                value
                        );

                if (!"meet.google.com"
                        .equalsIgnoreCase(
                                uri.getHost()
                        )) {

                    throw new BusinessRuleException(
                            "O link informado não pertence ao Google Meet."
                    );
                }

                value =
                        uri.getPath();

            } catch (IllegalArgumentException exception) {

                throw new BusinessRuleException(
                        "O link do Google Meet é inválido."
                );
            }
        }

        if (value.startsWith(
                "meet.google.com/"
        )) {

            value =
                    value.substring(
                            "meet.google.com/".length()
                    );
        }

        if (value.startsWith("/")) {

            value =
                    value.substring(1);
        }

        if (value.endsWith("/")) {

            value =
                    value.substring(
                            0,
                            value.length() - 1
                    );
        }

        value =
                value.toLowerCase(
                        Locale.ROOT
                );

        if (!MEETING_CODE_PATTERN
                .matcher(value)
                .matches()) {

            throw new BusinessRuleException(
                    "O código do Google Meet é inválido."
            );
        }

        return value;
    }
}