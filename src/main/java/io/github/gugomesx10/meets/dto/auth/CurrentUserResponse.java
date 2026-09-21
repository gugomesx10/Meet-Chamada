package io.github.gugomesx10.meets.dto.auth;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String externalId,
        String name,
        String email

) {
}