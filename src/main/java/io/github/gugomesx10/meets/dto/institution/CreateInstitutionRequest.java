package io.github.gugomesx10.meets.dto.institution;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInstitutionRequest(
        @NotBlank
        @Size(max = 150)
        String name

) {
}