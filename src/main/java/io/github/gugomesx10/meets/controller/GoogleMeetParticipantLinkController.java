package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetParticipantLinkResponse;
import io.github.gugomesx10.meets.dto.googlemeet.LinkGoogleMeetParticipantRequest;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.service.AuthenticatedUserService;
import io.github.gugomesx10.meets.service.GoogleMeetParticipantLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/class-sessions")
@RequiredArgsConstructor
@Profile("oauth")
public class GoogleMeetParticipantLinkController {

    private final GoogleMeetParticipantLinkService googleMeetParticipantLinkService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/{classSessionId}/google-meet/participant-links")
    public ResponseEntity<GoogleMeetParticipantLinkResponse> link(
            @PathVariable UUID classSessionId,
            @Valid @RequestBody LinkGoogleMeetParticipantRequest request
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var link =
                googleMeetParticipantLinkService.link(
                        classSessionId,
                        request.studentId(),
                        request.googleMeetUserName(),
                        currentUser
                );

        return ResponseEntity.ok(
                GoogleMeetParticipantLinkResponse.from(
                        link
                )
        );
    }
}