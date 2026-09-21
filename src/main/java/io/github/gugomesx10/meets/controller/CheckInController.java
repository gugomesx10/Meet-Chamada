package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.checkin.CheckInResponseDto;
import io.github.gugomesx10.meets.dto.checkin.CheckInWindowResponse;
import io.github.gugomesx10.meets.dto.checkin.OpenCheckInRequest;
import io.github.gugomesx10.meets.entity.CheckInResponse;
import io.github.gugomesx10.meets.entity.CheckInWindow;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.service.AuthenticatedUserService;
import io.github.gugomesx10.meets.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/check-ins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping
    public ResponseEntity<CheckInWindowResponse> open(
            @Valid @RequestBody OpenCheckInRequest request
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        CheckInWindow window =
                checkInService.openCheckIn(
                        request.classSessionId(),
                        request.sessionBlockId(),
                        currentUser,
                        Duration.ofMinutes(
                                request.durationMinutes()
                        )
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        CheckInWindowResponse.from(
                                window
                        )
                );
    }

    @PostMapping("/{checkInId}/responses")
    public ResponseEntity<CheckInResponseDto> respond(
            @PathVariable UUID checkInId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        CheckInResponse response =
                checkInService.respond(
                        checkInId,
                        currentUser
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        CheckInResponseDto.from(
                                response
                        )
                );
    }

    @PatchMapping("/{checkInId}/close")
    public ResponseEntity<CheckInWindowResponse> close(
            @PathVariable UUID checkInId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        CheckInWindow window =
                checkInService.closeCheckIn(
                        checkInId,
                        currentUser
                );

        return ResponseEntity.ok(
                CheckInWindowResponse.from(
                        window
                )
        );
    }

    @PatchMapping("/{checkInId}/cancel")
    public ResponseEntity<CheckInWindowResponse> cancel(
            @PathVariable UUID checkInId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        CheckInWindow window =
                checkInService.cancelCheckIn(
                        checkInId,
                        currentUser
                );

        return ResponseEntity.ok(
                CheckInWindowResponse.from(
                        window
                )
        );
    }

    @GetMapping("/{checkInId}/responses")
    public ResponseEntity<List<CheckInResponseDto>> responses(
            @PathVariable UUID checkInId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        List<CheckInResponseDto> responses =
                checkInService
                        .findResponses(
                                checkInId,
                                currentUser
                        )
                        .stream()
                        .map(CheckInResponseDto::from)
                        .toList();

        return ResponseEntity.ok(
                responses
        );
    }

    @GetMapping("/sessions/{classSessionId}")
    public ResponseEntity<List<CheckInWindowResponse>> bySession(
            @PathVariable UUID classSessionId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        List<CheckInWindowResponse> checkIns =
                checkInService
                        .findBySession(
                                classSessionId,
                                currentUser
                        )
                        .stream()
                        .map(CheckInWindowResponse::from)
                        .toList();

        return ResponseEntity.ok(
                checkIns
        );
    }
}