package io.github.gugomesx10.meets.dto.checkin;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RespondCheckInRequest(
        @NotNull
        UUID studentId
) {
}