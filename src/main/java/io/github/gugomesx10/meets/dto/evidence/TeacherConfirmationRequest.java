package io.github.gugomesx10.meets.dto.evidence;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record TeacherConfirmationRequest(
        @NotNull
        UUID studentId,
        UUID sessionBlockId,
        @NotNull
        UUID teacherId,
        @Size(max = 1000)
        String details

) {
}