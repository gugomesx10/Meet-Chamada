package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.InstitutionMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstitutionMembershipRepository extends JpaRepository<InstitutionMembership, UUID> {
    Optional<InstitutionMembership> findByUserIdAndInstitutionId(
            UUID userId,
            UUID institutionId
    );
    List<InstitutionMembership> findAllByInstitutionId(UUID institutionId);
    List<InstitutionMembership> findAllByUserId(UUID userId);
    boolean existsByUserIdAndInstitutionId(
            UUID userId,
            UUID institutionId
    );
}