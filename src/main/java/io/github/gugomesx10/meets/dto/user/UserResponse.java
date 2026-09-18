package io.github.gugomesx10.meets.dto.user;

import io.github.gugomesx10.meets.entity.User;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String externalId,
        Instant createdAt,
        Instant updatedAt

) {

    public static UserResponse from(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getExternalId(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}