package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetParticipantSessionsResponse;
import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetParticipantsResponse;
import io.github.gugomesx10.meets.dto.googlemeet.GoogleMeetSpaceResponse;
import io.github.gugomesx10.meets.service.GoogleMeetService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/google-meet")
@RequiredArgsConstructor
@Profile("oauth")
public class GoogleMeetController {

    private final GoogleMeetService googleMeetService;

    @GetMapping("/spaces")
    public ResponseEntity<GoogleMeetSpaceResponse> findSpace(
            @RequestParam String reference
    ) {

        return ResponseEntity.ok(
                googleMeetService.findSpace(
                        reference
                )
        );
    }

    @GetMapping("/participants")
    public ResponseEntity<GoogleMeetParticipantsResponse> findParticipants(
            @RequestParam String conferenceRecord
    ) {

        return ResponseEntity.ok(
                googleMeetService.findParticipants(
                        conferenceRecord
                )
        );
    }

    @GetMapping("/participant-sessions")
    public ResponseEntity<GoogleMeetParticipantSessionsResponse> findParticipantSessions(
            @RequestParam String participant
    ) {

        return ResponseEntity.ok(
                googleMeetService.findParticipantSessions(
                        participant
                )
        );
    }
}