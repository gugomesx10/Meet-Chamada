package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import io.github.gugomesx10.meets.exception.ForbiddenOperationException;
import io.github.gugomesx10.meets.repository.InstitutionMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final InstitutionMembershipRepository institutionMembershipRepository;

    public InstitutionMembership requireInstitutionMember(
            User user,
            UUID institutionId
    ) {

        return institutionMembershipRepository
                .findByUserIdAndInstitutionId(
                        user.getId(),
                        institutionId
                )
                .orElseThrow(() ->
                        new ForbiddenOperationException(
                                "O usuário não pertence a esta instituição."
                        )
                );
    }

    public InstitutionMembership requireInstitutionAdmin(
            User user,
            UUID institutionId
    ) {

        InstitutionMembership membership =
                requireInstitutionMember(
                        user,
                        institutionId
                );

        if (membership.getRole() != InstitutionRole.ADMIN) {

            throw new ForbiddenOperationException(
                    "Somente um administrador da instituição pode executar esta operação."
            );
        }

        return membership;
    }

    public void requireInstitutionAdminOrSelf(
            User user,
            UUID institutionId,
            UUID targetUserId
    ) {

        if (user.getId().equals(targetUserId)) {

            requireInstitutionMember(
                    user,
                    institutionId
            );

            return;
        }

        requireInstitutionAdmin(
                user,
                institutionId
        );
    }
}