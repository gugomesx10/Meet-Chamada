package io.github.gugomesx10.meets.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreateCourseRequest(
        @NotNull
        UUID institutionId,
        @NotBlank
        @Size(max = 150)
        String name,
        @Size(max = 1000)
        String description,
        @NotNull
        LocalDate startDate,
        @NotNull
        LocalDate endDate

) {
}