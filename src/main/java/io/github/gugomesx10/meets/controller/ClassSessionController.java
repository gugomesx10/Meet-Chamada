package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.session.ClassSessionResponse;
import io.github.gugomesx10.meets.dto.session.CreateClassSessionRequest;
import io.github.gugomesx10.meets.service.ClassSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/class-sessions")
@RequiredArgsConstructor
public class ClassSessionController {

    private final ClassSessionService classSessionService;

    @PostMapping
    public ResponseEntity<ClassSessionResponse> create(
            @Valid @RequestBody CreateClassSessionRequest request
    ) {

        var session = classSessionService.create(
                request.courseId(),
                request.title(),
                request.sessionDate(),
                request.startTime(),
                request.endTime()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ClassSessionResponse.from(session)
                );
    }

    @GetMapping("/{classSessionId}")
    public ResponseEntity<ClassSessionResponse> findById(
            @PathVariable UUID classSessionId
    ) {

        var session =
                classSessionService.findById(
                        classSessionId
                );

        return ResponseEntity.ok(
                ClassSessionResponse.from(session)
        );
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<ClassSessionResponse>> findByCourse(
            @PathVariable UUID courseId
    ) {

        var sessions =
                classSessionService
                        .findByCourse(courseId)
                        .stream()
                        .map(ClassSessionResponse::from)
                        .toList();

        return ResponseEntity.ok(sessions);
    }

    @PatchMapping("/{classSessionId}/start")
    public ResponseEntity<ClassSessionResponse> start(
            @PathVariable UUID classSessionId
    ) {

        var session =
                classSessionService.start(
                        classSessionId
                );

        return ResponseEntity.ok(
                ClassSessionResponse.from(session)
        );
    }

    @PatchMapping("/{classSessionId}/complete")
    public ResponseEntity<ClassSessionResponse> complete(
            @PathVariable UUID classSessionId
    ) {

        var session =
                classSessionService.complete(
                        classSessionId
                );

        return ResponseEntity.ok(
                ClassSessionResponse.from(session)
        );
    }

    @PatchMapping("/{classSessionId}/cancel")
    public ResponseEntity<ClassSessionResponse> cancel(
            @PathVariable UUID classSessionId
    ) {

        var session =
                classSessionService.cancel(
                        classSessionId
                );

        return ResponseEntity.ok(
                ClassSessionResponse.from(session)
        );
    }
}