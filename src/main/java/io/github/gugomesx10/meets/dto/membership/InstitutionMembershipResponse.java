package io.github.gugomesx10.meets.dto.membership;

import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import java.time.Instant;
import java.util.UUID;

public record InstitutionMembershipResponse(
        UUID id,
        UUID institutionId,
        UUID userId,
        InstitutionRole role,
        Instant createdAt,
        Instant updatedAt

) {

    public static InstitutionMembershipResponse from(
            InstitutionMembership membership
    ) {

        return new InstitutionMembershipResponse(
                membership.getId(),
                membership.getInstitution().getId(),
                membership.getUser().getId(),
                membership.getRole(),
                membership.getCreatedAt(),
                membership.getUpdatedAt()
        );
    }
}