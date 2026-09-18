package io.github.gugomesx10.meets.dto.membership;

import io.github.gugomesx10.meets.entity.CourseMembership;
import io.github.gugomesx10.meets.entity.enums.CourseRole;
import java.time.Instant;
import java.util.UUID;

public record CourseMembershipResponse(

        UUID id,
        UUID courseId,
        UUID userId,
        CourseRole role,
        Instant createdAt,
        Instant updatedAt

) {

    public static CourseMembershipResponse from(
            CourseMembership membership
    ) {

        return new CourseMembershipResponse(
                membership.getId(),
                membership.getCourse().getId(),
                membership.getUser().getId(),
                membership.getRole(),
                membership.getCreatedAt(),
                membership.getUpdatedAt()
        );
    }
}