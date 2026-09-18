package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.sessionblock.CreateSessionBlockRequest;
import io.github.gugomesx10.meets.dto.sessionblock.SessionBlockResponse;
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

    @PostMapping("/sessions/{classSessionId}")
    public ResponseEntity<SessionBlockResponse> create(
            @PathVariable UUID classSessionId,
            @Valid @RequestBody CreateSessionBlockRequest request
    ) {

        var block = sessionBlockService.create(
                classSessionId,
                request.instructorId(),
                request.title(),
                request.description(),
                request.type(),
                request.startTime(),
                request.endTime()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SessionBlockResponse.from(block));
    }

    @GetMapping("/{sessionBlockId}")
    public ResponseEntity<SessionBlockResponse> findById(
            @PathVariable UUID sessionBlockId
    ) {

        var block =
                sessionBlockService.findById(
                        sessionBlockId
                );

        return ResponseEntity.ok(
                SessionBlockResponse.from(block)
        );
    }

    @GetMapping("/sessions/{classSessionId}")
    public ResponseEntity<List<SessionBlockResponse>> findBySession(
            @PathVariable UUID classSessionId
    ) {

        var blocks =
                sessionBlockService
                        .findBySession(classSessionId)
                        .stream()
                        .map(SessionBlockResponse::from)
                        .toList();

        return ResponseEntity.ok(blocks);
    }

    @GetMapping("/instructors/{instructorId}")
    public ResponseEntity<List<SessionBlockResponse>> findByInstructor(
            @PathVariable UUID instructorId
    ) {

        var blocks =
                sessionBlockService
                        .findByInstructor(instructorId)
                        .stream()
                        .map(SessionBlockResponse::from)
                        .toList();

        return ResponseEntity.ok(blocks);
    }
}