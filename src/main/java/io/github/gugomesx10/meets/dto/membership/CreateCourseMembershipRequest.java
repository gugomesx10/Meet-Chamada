package io.github.gugomesx10.meets.dto.membership;

import io.github.gugomesx10.meets.entity.enums.CourseRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCourseMembershipRequest(
        @NotNull
        UUID userId,
        @NotNull
        CourseRole role

) {
}