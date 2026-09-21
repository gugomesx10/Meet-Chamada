package io.github.gugomesx10.meets.dto.googlemeet;

public record GoogleMeetSpaceResponse(
        String name,
        String meetingUri,
        String meetingCode,
        ActiveConferenceResponse activeConference
) {

    public record ActiveConferenceResponse(
            String conferenceRecord
    ) {
    }
}