package io.github.gugomesx10.meets.dto.googlemeet;

import java.time.Instant;
import java.util.List;

public record GoogleMeetParticipantSessionsResponse(
        List<ParticipantSessionResponse> participantSessions,
        String nextPageToken
) {

    public record ParticipantSessionResponse(
            String name,
            Instant startTime,
            Instant endTime
    ) {
    }
}