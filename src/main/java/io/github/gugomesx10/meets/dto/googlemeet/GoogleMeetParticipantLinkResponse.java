package io.github.gugomesx10.meets.dto.googlemeet;

import io.github.gugomesx10.meets.entity.GoogleMeetParticipantLink;
import java.time.Instant;
import java.util.UUID;

public record GoogleMeetParticipantLinkResponse(
        UUID id,
        UUID userId,
        String googleMeetUserName,
        Instant createdAt,
        Instant updatedAt
) {

    public static GoogleMeetParticipantLinkResponse from(
            GoogleMeetParticipantLink link
    ) {

        return new GoogleMeetParticipantLinkResponse(
                link.getId(),
                link.getUser().getId(),
                link.getGoogleMeetUserName(),
                link.getCreatedAt(),
                link.getUpdatedAt()
        );
    }
}