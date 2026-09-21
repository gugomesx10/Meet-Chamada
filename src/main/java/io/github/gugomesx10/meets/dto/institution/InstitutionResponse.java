package io.github.gugomesx10.meets.dto.institution;

import io.github.gugomesx10.meets.entity.Institution;
import java.time.Instant;
import java.util.UUID;

public record InstitutionResponse(

        UUID id,
        String name,
        Instant createdAt,
        Instant updatedAt

) {

    public static InstitutionResponse from(
            Institution institution
    ) {

        return new InstitutionResponse(
                institution.getId(),
                institution.getName(),
                institution.getCreatedAt(),
                institution.getUpdatedAt()
        );
    }
}