package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InstitutionRepository extends JpaRepository<Institution, UUID> {
}