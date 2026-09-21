package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.Institution;
import io.github.gugomesx10.meets.entity.InstitutionMembership;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.InstitutionRole;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
import io.github.gugomesx10.meets.repository.InstitutionMembershipRepository;
import io.github.gugomesx10.meets.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final InstitutionMembershipRepository institutionMembershipRepository;
    private final AuthorizationService authorizationService;
    @Transactional
    public Institution create(
            String name,
            User creator
    ) {

        if (name == null || name.isBlank()) {

            throw new BusinessRuleException(
                    "O nome da instituição é obrigatório."
            );
        }

        Institution institution =
                new Institution();

        institution.setName(
                name.trim()
        );

        Institution savedInstitution =
                institutionRepository.save(
                        institution
                );

        InstitutionMembership adminMembership =
                new InstitutionMembership();

        adminMembership.setInstitution(
                savedInstitution
        );

        adminMembership.setUser(
                creator
        );

        adminMembership.setRole(
                InstitutionRole.ADMIN
        );

        institutionMembershipRepository.save(
                adminMembership
        );

        return savedInstitution;
    }
    @Transactional(readOnly = true)
    public Institution findById(
            UUID institutionId,
            User currentUser
    ) {

        Institution institution =
                institutionRepository
                        .findById(institutionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Instituição não encontrada."
                                )
                        );

        authorizationService.requireInstitutionMember(
                currentUser,
                institutionId
        );

        return institution;
    }
    @Transactional(readOnly = true)
    public List<Institution> findAllByUser(
            User currentUser
    ) {

        return institutionMembershipRepository
                .findAllByUserId(
                        currentUser.getId()
                )
                .stream()
                .map(
                        InstitutionMembership::getInstitution
                )
                .toList();
    }
}