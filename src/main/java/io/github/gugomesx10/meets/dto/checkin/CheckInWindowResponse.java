package io.github.gugomesx10.meets.dto.checkin;

import io.github.gugomesx10.meets.entity.CheckInWindow;
import io.github.gugomesx10.meets.entity.enums.CheckInStatus;
import java.time.Instant;
import java.util.UUID;

public record CheckInWindowResponse(
        UUID id,
        UUID classSessionId,
        UUID sessionBlockId,
        UUID openedById,
        Instant openedAt,
        Instant closesAt,
        CheckInStatus status
) {

    public static CheckInWindowResponse from(
            CheckInWindow window
    ) {
        return new CheckInWindowResponse(
                window.getId(),
                window.getClassSession().getId(),

                window.getSessionBlock() != null
                        ? window.getSessionBlock().getId()
                        : null,

                window.getOpenedBy().getId(),
                window.getOpenedAt(),
                window.getClosesAt(),
                window.getStatus()
        );
    }
}