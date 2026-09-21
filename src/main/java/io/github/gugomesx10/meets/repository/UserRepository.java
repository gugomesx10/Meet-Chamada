package io.github.gugomesx10.meets.repository;

import io.github.gugomesx10.meets.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    Optional<User> findByExternalId(String externalId);
    boolean existsByEmail(String email);
}