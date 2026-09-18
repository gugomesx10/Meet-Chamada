package io.github.gugomesx10.meets.service;

import io.github.gugomesx10.meets.entity.Institution;
import io.github.gugomesx10.meets.exception.BusinessRuleException;
import io.github.gugomesx10.meets.exception.ResourceNotFoundException;
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
    @Transactional
    public Institution create(String name) {

        if (name == null || name.isBlank()) {
            throw new BusinessRuleException(
                    "O nome da instituição é obrigatório."
            );
        }

        Institution institution =
                new Institution();

        institution.setName(name.trim());

        return institutionRepository.save(
                institution
        );
    }
    @Transactional(readOnly = true)
    public Institution findById(UUID institutionId) {

        return institutionRepository
                .findById(institutionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Instituição não encontrada."
                        )
                );
    }
    @Transactional(readOnly = true)
    public List<Institution> findAll() {
        return institutionRepository.findAll();
    }
}