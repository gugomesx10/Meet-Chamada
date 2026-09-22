package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.Institution;
import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ConflictException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.InstitutionMembershipRepository;
import io.github.gugomesx10.meets.repository.InstitutionRepository;
import io.github.gugomesx10.meets.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstitutionMembershipService {

    private final InstitutionMembershipRepository institutionMembershipRepository;
    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    @Transactional
    public InstitutionMembership create(
            UUID institutionId,
            UUID userId,
            InstitutionRole role,
            User currentUser
    ) {

        authorizationService.requireInstitutionAdmin(
                currentUser,
                institutionId
        );

        Institution institution =
                institutionRepository
                        .findById(institutionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Instituição não encontrada."
                                )
                        );

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Usuário não encontrado."
                                )
                        );

        if (role == null) {
            throw new BusinessRuleException(
                    "O papel do usuário na instituição é obrigatório."
            );
        }

        if (institutionMembershipRepository
                .existsByUserIdAndInstitutionId(
                        userId,
                        institutionId
                )) {

            throw new ConflictException(
                    "O usuário já pertence a esta instituição."
            );
        }

        InstitutionMembership membership =
                new InstitutionMembership();

        membership.setInstitution(institution);
        membership.setUser(user);
        membership.setRole(role);

        InstitutionMembership saved =
                institutionMembershipRepository.save(
                        membership
                );

        auditService.register(
                currentUser,
                "INSTITUTION_MEMBERSHIP_CREATED",
                "InstitutionMembership",
                saved.getId(),
                "Vínculo institucional criado para o usuário "
                        + userId
                        + " com papel "
                        + role
                        + "."
        );

        return saved;
    }
    @Transactional
    public InstitutionMembership updateRole(
            UUID institutionId,
            UUID userId,
            InstitutionRole newRole,
            User currentUser
    ) {

        authorizationService.requireInstitutionAdmin(
                currentUser,
                institutionId
        );

        if (newRole == null) {
            throw new BusinessRuleException(
                    "O novo papel do usuário na instituição é obrigatório."
            );
        }

        InstitutionMembership membership =
                institutionMembershipRepository
                        .findByUserIdAndInstitutionId(
                                userId,
                                institutionId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Vínculo institucional não encontrado."
                                )
                        );

        InstitutionRole currentRole =
                membership.getRole();

        if (currentRole == newRole) {
            return membership;
        }

        if (currentRole == InstitutionRole.ADMIN
                && newRole != InstitutionRole.ADMIN) {

            long adminCount =
                    institutionMembershipRepository
                            .countByInstitutionIdAndRole(
                                    institutionId,
                                    InstitutionRole.ADMIN
                            );

            if (adminCount <= 1) {
                throw new BusinessRuleException(
                        "Não é possível alterar o papel do último administrador da instituição."
                );
            }
        }

        membership.setRole(
                newRole
        );

        InstitutionMembership saved =
                institutionMembershipRepository.save(
                        membership
                );

        auditService.register(
                currentUser,
                "INSTITUTION_MEMBERSHIP_ROLE_UPDATED",
                "InstitutionMembership",
                saved.getId(),
                "Papel institucional do usuário "
                        + userId
                        + " alterado de "
                        + currentRole
                        + " para "
                        + newRole
                        + "."
        );

        return saved;
    }
    @Transactional(readOnly = true)
    public InstitutionMembership find(
            UUID institutionId,
            UUID userId,
            User currentUser
    ) {

        authorizationService.requireInstitutionAdminOrSelf(
                currentUser,
                institutionId,
                userId
        );

        return institutionMembershipRepository
                .findByUserIdAndInstitutionId(
                        userId,
                        institutionId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vínculo institucional não encontrado."
                        )
                );
    }
    @Transactional(readOnly = true)
    public List<InstitutionMembership> findByInstitution(
            UUID institutionId,
            User currentUser
    ) {

        if (!institutionRepository.existsById(
                institutionId
        )) {

            throw new ResourceNotFoundException(
                    "Instituição não encontrada."
            );
        }

        authorizationService.requireInstitutionAdmin(
                currentUser,
                institutionId
        );

        return institutionMembershipRepository
                .findAllByInstitutionId(
                        institutionId
                );
    }
    @Transactional(readOnly = true)
    public List<InstitutionMembership> findByUser(
            UUID userId
    ) {

        if (!userRepository.existsById(
                userId
        )) {

            throw new ResourceNotFoundException(
                    "Usuário não encontrado."
            );
        }

        return institutionMembershipRepository
                .findAllByUserId(
                        userId
                );
    }
}