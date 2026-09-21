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

        membership.setInstitution(
                institution
        );

        membership.setUser(
                user
        );

        membership.setRole(
                role
        );

        return institutionMembershipRepository.save(
                membership
        );
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