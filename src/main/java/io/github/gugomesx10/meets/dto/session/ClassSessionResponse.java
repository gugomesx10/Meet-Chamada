package io.github.gugomesx10.meets.dto.session;

import io.github.gugomesx10.meets.entity.ClassSession;
import io.github.gugomesx10.meets.entity.enums.ClassSessionStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ClassSessionResponse(
        UUID id,
        UUID courseId,
        String title,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        ClassSessionStatus status,
        String googleMeetSpaceName,
        String googleMeetConferenceRecordName,
        Instant createdAt,
        Instant updatedAt
) {

    public static ClassSessionResponse from(
            ClassSession session
    ) {

        return new ClassSessionResponse(
                session.getId(),
                session.getCourse().getId(),
                session.getTitle(),
                session.getSessionDate(),
                session.getStartTime(),
                session.getEndTime(),
                session.getStatus(),
                session.getGoogleMeetSpaceName(),
                session.getGoogleMeetConferenceRecordName(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}