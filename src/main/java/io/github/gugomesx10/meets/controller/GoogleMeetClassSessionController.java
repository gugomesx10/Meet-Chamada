package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetSyncResponse;
import io.github.gugomesx10.meets.dto.googlemeet.LinkGoogleMeetRequest;
import io.github.gugomesx10.meets.dto.session.ClassSessionResponse;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.service.AuthenticatedUserService;
import io.github.gugomesx10.meets.service.GoogleMeetClassSessionService;
import io.github.gugomesx10.meets.service.GoogleMeetSyncService;
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
public class GoogleMeetClassSessionController {
    private final GoogleMeetClassSessionService googleMeetClassSessionService;
    private final GoogleMeetSyncService googleMeetSyncService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/{classSessionId}/google-meet")
    public ResponseEntity<ClassSessionResponse> linkGoogleMeet(
            @PathVariable UUID classSessionId,
            @Valid @RequestBody LinkGoogleMeetRequest request
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var classSession =
                googleMeetClassSessionService.link(
                        classSessionId,
                        request.reference(),
                        currentUser
                );

        return ResponseEntity.ok(
                ClassSessionResponse.from(
                        classSession
                )
        );
    }

    @PostMapping("/{classSessionId}/google-meet/sync")
    public ResponseEntity<GoogleMeetSyncResponse> syncGoogleMeet(
            @PathVariable UUID classSessionId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        return ResponseEntity.ok(
                googleMeetSyncService.sync(
                        classSessionId,
                        currentUser
                )
        );
    }
}