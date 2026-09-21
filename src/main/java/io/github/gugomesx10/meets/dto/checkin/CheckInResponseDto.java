package io.github.gugomesx10.meets.dto.checkin;

import io.github.gugomesx10.meets.entity.CheckInResponse;
import java.time.Instant;
import java.util.UUID;

public record CheckInResponseDto(
        UUID id,
        UUID checkInWindowId,
        UUID studentId,
        Instant respondedAt,
        boolean valid
) {

    public static CheckInResponseDto from(
            CheckInResponse response
    ) {
        return new CheckInResponseDto(
                response.getId(),
                response.getCheckInWindow().getId(),
                response.getStudent().getId(),
                response.getRespondedAt(),
                response.isValid()
        );
    }
}