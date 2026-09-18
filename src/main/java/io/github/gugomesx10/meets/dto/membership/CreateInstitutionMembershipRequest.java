package io.github.gugomesx10.meets.dto.membership;

import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateInstitutionMembershipRequest(
        @NotNull
        UUID userId,
        @NotNull
        InstitutionRole role

) {
}