package io.github.gugomesx10.meets.dto.googlemeet;

import java.util.UUID;

public record GoogleMeetSyncResponse(
        UUID classSessionId,
        String conferenceRecordName,
        int participantsFound,
        int mappedParticipants,
        int unmappedParticipants,
        int unsupportedParticipants,
        int linkedUsersOutsideCourse,
        int sessionsImported,
        int sessionsAlreadyImported
) {
}