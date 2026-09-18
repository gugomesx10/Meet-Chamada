package io.github.gugomesx10.meets.dto.attendance;

import io.github.gugomesx10.meets.entity.enums.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ReviewAttendanceRequest(
        @NotNull
        UUID reviewerId,
        @NotNull
        AttendanceStatus newStatus,
        @NotBlank
        @Size(max = 2000)
        String reason

) {
}