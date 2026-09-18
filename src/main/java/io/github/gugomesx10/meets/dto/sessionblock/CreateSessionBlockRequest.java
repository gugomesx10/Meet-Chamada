package io.github.gugomesx10.meets.dto.sessionblock;

import io.github.gugomesx10.meets.entity.enums.SessionBlockType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.UUID;

public record CreateSessionBlockRequest(
        @NotNull
        UUID instructorId,
        @NotBlank
        @Size(max = 150)
        String title,
        @Size(max = 1000)
        String description,
        @NotNull
        SessionBlockType type,
        @NotNull
        LocalTime startTime,
        @NotNull
        LocalTime endTime

) {
}