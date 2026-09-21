package io.github.gugomesx10.meets.dto.googlemeet;

import java.time.Instant;
import java.util.List;

public record GoogleMeetParticipantsResponse(
        List<ParticipantResponse> participants,
        String nextPageToken
) {

    public record ParticipantResponse(
            String name,
            SignedInUser signedinUser,
            AnonymousUser anonymousUser,
            PhoneUser phoneUser,
            Instant earliestStartTime,
            Instant latestEndTime
    ) {
    }

    public record SignedInUser(
            String user,
            String displayName
    ) {
    }

    public record AnonymousUser(
            String displayName
    ) {
    }

    public record PhoneUser(
            String displayName
    ) {
    }
}