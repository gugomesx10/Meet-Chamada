package io.github.gugomesx10.meets.dto.membership;

import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import jakarta.validation.constraints.NotNull;

public record UpdateInstitutionMembershipRoleRequest(
        @NotNull
        InstitutionRole role

) {
}