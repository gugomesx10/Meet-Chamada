package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.course.CourseResponse;
import io.github.gugomesx10.meets.dto.course.CreateCourseRequest;
import io.github.gugomesx10.meets.service.CourseService;
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
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponse> create(
            @Valid @RequestBody CreateCourseRequest request
    ) {

        var course =
                courseService.create(
                        request.institutionId(),
                        request.name(),
                        request.description(),
                        request.startDate(),
                        request.endDate()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CourseResponse.from(course));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> findById(
            @PathVariable UUID courseId
    ) {

        return ResponseEntity.ok(
                CourseResponse.from(
                        courseService.findById(courseId)
                )
        );
    }

    @GetMapping("/institutions/{institutionId}")
    public ResponseEntity<List<CourseResponse>> findByInstitution(
            @PathVariable UUID institutionId
    ) {

        var courses =
                courseService
                        .findByInstitution(institutionId)
                        .stream()
                        .map(CourseResponse::from)
                        .toList();

        return ResponseEntity.ok(courses);
    }

    @PatchMapping("/{courseId}/activate")
    public ResponseEntity<CourseResponse> activate(
            @PathVariable UUID courseId
    ) {

        return ResponseEntity.ok(
                CourseResponse.from(
                        courseService.activate(courseId)
                )
        );
    }

    @PatchMapping("/{courseId}/complete")
    public ResponseEntity<CourseResponse> complete(
            @PathVariable UUID courseId
    ) {

        return ResponseEntity.ok(
                CourseResponse.from(
                        courseService.complete(courseId)
                )
        );
    }

    @PatchMapping("/{courseId}/cancel")
    public ResponseEntity<CourseResponse> cancel(
            @PathVariable UUID courseId
    ) {

        return ResponseEntity.ok(
                CourseResponse.from(
                        courseService.cancel(courseId)
                )
        );
    }
}