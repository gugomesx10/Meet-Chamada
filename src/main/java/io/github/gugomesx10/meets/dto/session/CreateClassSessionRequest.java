package io.github.gugomesx10.meets.dto.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateClassSessionRequest(
        @NotNull
        UUID courseId,
        @NotBlank
        @Size(max = 150)
        String title,
        @NotNull
        LocalDate sessionDate,
        @NotNull
        LocalTime startTime,
        @NotNull
        LocalTime endTime

) {
}