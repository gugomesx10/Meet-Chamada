package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.sessionblock.CreateSessionBlockRequest;
import io.github.gugomesx10.meets.dto.sessionblock.SessionBlockResponse;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.service.AuthenticatedUserService;
import io.github.gugomesx10.meets.service.SessionBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/session-blocks")
@RequiredArgsConstructor
public class SessionBlockController {

    private final SessionBlockService sessionBlockService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/sessions/{classSessionId}")
    public ResponseEntity<SessionBlockResponse> create(
            @PathVariable UUID classSessionId,
            @Valid @RequestBody CreateSessionBlockRequest request
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var block =
                sessionBlockService.create(
                        classSessionId,
                        request.instructorId(),
                        request.title(),
                        request.description(),
                        request.type(),
                        request.startTime(),
                        request.endTime(),
                        currentUser
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        SessionBlockResponse.from(block)
                );
    }

    @GetMapping("/{sessionBlockId}")
    public ResponseEntity<SessionBlockResponse> findById(
            @PathVariable UUID sessionBlockId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var block =
                sessionBlockService.findById(
                        sessionBlockId,
                        currentUser
                );

        return ResponseEntity.ok(
                SessionBlockResponse.from(block)
        );
    }

    @GetMapping("/sessions/{classSessionId}")
    public ResponseEntity<List<SessionBlockResponse>> findBySession(
            @PathVariable UUID classSessionId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var blocks =
                sessionBlockService
                        .findBySession(
                                classSessionId,
                                currentUser
                        )
                        .stream()
                        .map(SessionBlockResponse::from)
                        .toList();

        return ResponseEntity.ok(blocks);
    }

    @GetMapping("/instructors/{instructorId}")
    public ResponseEntity<List<SessionBlockResponse>> findByInstructor(
            @PathVariable UUID instructorId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var blocks =
                sessionBlockService
                        .findByInstructor(
                                instructorId,
                                currentUser
                        )
                        .stream()
                        .map(SessionBlockResponse::from)
                        .toList();

        return ResponseEntity.ok(blocks);
    }
}