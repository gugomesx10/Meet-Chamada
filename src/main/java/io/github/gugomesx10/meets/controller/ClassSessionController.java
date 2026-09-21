package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.session.ClassSessionResponse;
import io.github.gugomesx10.meets.dto.session.CreateClassSessionRequest;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.service.AuthenticatedUserService;
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
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping
    public ResponseEntity<ClassSessionResponse> create(
            @Valid @RequestBody CreateClassSessionRequest request
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var session =
                classSessionService.create(
                        request.courseId(),
                        request.title(),
                        request.sessionDate(),
                        request.startTime(),
                        request.endTime(),
                        currentUser
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

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var session =
                classSessionService.findById(
                        classSessionId,
                        currentUser
                );

        return ResponseEntity.ok(
                ClassSessionResponse.from(session)
        );
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<ClassSessionResponse>> findByCourse(
            @PathVariable UUID courseId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var sessions =
                classSessionService
                        .findByCourse(
                                courseId,
                                currentUser
                        )
                        .stream()
                        .map(ClassSessionResponse::from)
                        .toList();

        return ResponseEntity.ok(sessions);
    }

    @PatchMapping("/{classSessionId}/start")
    public ResponseEntity<ClassSessionResponse> start(
            @PathVariable UUID classSessionId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var session =
                classSessionService.start(
                        classSessionId,
                        currentUser
                );

        return ResponseEntity.ok(
                ClassSessionResponse.from(session)
        );
    }

    @PatchMapping("/{classSessionId}/complete")
    public ResponseEntity<ClassSessionResponse> complete(
            @PathVariable UUID classSessionId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var session =
                classSessionService.complete(
                        classSessionId,
                        currentUser
                );

        return ResponseEntity.ok(
                ClassSessionResponse.from(session)
        );
    }

    @PatchMapping("/{classSessionId}/cancel")
    public ResponseEntity<ClassSessionResponse> cancel(
            @PathVariable UUID classSessionId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var session =
                classSessionService.cancel(
                        classSessionId,
                        currentUser
                );

        return ResponseEntity.ok(
                ClassSessionResponse.from(session)
        );
    }
}