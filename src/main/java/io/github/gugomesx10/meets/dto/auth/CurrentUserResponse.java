package io.github.gugomesx10.meets.dto.auth;

public record CurrentUserResponse(
        String externalId,
        String name,
        String email

) {
}