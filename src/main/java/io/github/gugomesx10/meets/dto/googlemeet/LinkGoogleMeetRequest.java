package io.github.gugomesx10.meets.dto.googlemeet;

import jakarta.validation.constraints.NotBlank;

public record LinkGoogleMeetRequest(
        @NotBlank
        String reference

) {
}