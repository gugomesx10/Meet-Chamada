package io.github.gugomesx10.meets.dto.googlemeet;

import java.time.Instant;
import java.util.List;

public record GoogleMeetConferenceRecordsResponse(
        List<ConferenceRecordResponse> conferenceRecords,
        String nextPageToken
) {

    public record ConferenceRecordResponse(
            String name,
            Instant startTime,
            Instant endTime
    ) {
    }
}