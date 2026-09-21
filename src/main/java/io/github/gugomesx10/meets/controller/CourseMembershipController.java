package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.membership.CourseMembershipResponse;
import io.github.gugomesx10.meets.dto.membership.CreateCourseMembershipRequest;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.service.AuthenticatedUserService;
import io.github.gugomesx10.meets.service.CourseMembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseMembershipController {

    private final CourseMembershipService courseMembershipService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/{courseId}/memberships")
    public ResponseEntity<CourseMembershipResponse> create(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateCourseMembershipRequest request
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var membership =
                courseMembershipService.create(
                        courseId,
                        request.userId(),
                        request.role(),
                        currentUser
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        CourseMembershipResponse.from(
                                membership
                        )
                );
    }

    @GetMapping("/{courseId}/memberships")
    public ResponseEntity<List<CourseMembershipResponse>> findByCourse(
            @PathVariable UUID courseId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var memberships =
                courseMembershipService
                        .findByCourse(
                                courseId,
                                currentUser
                        )
                        .stream()
                        .map(CourseMembershipResponse::from)
                        .toList();

        return ResponseEntity.ok(
                memberships
        );
    }

    @GetMapping("/{courseId}/memberships/{userId}")
    public ResponseEntity<CourseMembershipResponse> find(
            @PathVariable UUID courseId,
            @PathVariable UUID userId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var membership =
                courseMembershipService.find(
                        courseId,
                        userId,
                        currentUser
                );

        return ResponseEntity.ok(
                CourseMembershipResponse.from(
                        membership
                )
        );
    }
}