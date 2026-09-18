package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.membership.CourseMembershipResponse;
import io.github.gugomesx10.meets.dto.membership.CreateCourseMembershipRequest;
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

    @PostMapping("/{courseId}/memberships")
    public ResponseEntity<CourseMembershipResponse> create(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateCourseMembershipRequest request
    ) {

        var membership =
                courseMembershipService.create(
                        courseId,
                        request.userId(),
                        request.role()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CourseMembershipResponse.from(membership));
    }

    @GetMapping("/{courseId}/memberships")
    public ResponseEntity<List<CourseMembershipResponse>> findByCourse(
            @PathVariable UUID courseId
    ) {

        var memberships =
                courseMembershipService
                        .findByCourse(courseId)
                        .stream()
                        .map(CourseMembershipResponse::from)
                        .toList();

        return ResponseEntity.ok(memberships);
    }

    @GetMapping("/{courseId}/memberships/{userId}")
    public ResponseEntity<CourseMembershipResponse> find(
            @PathVariable UUID courseId,
            @PathVariable UUID userId
    ) {

        var membership =
                courseMembershipService.find(
                        courseId,
                        userId
                );

        return ResponseEntity.ok(
                CourseMembershipResponse.from(membership)
        );
    }
}