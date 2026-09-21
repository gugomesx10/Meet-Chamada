package io.github.gugomesx10.meets.dto.checkin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record OpenCheckInRequest(
        @NotNull
        UUID classSessionId,
        UUID sessionBlockId,
        @Positive
        long durationMinutes

) {
}