package io.github.gugomesx10.meets.dto.googlemeet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LinkGoogleMeetParticipantRequest(
        @NotNull
        UUID studentId,
        @NotBlank
        String googleMeetUserName

) {
}