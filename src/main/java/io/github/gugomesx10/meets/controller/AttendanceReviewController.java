package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.attendance.AttendanceReviewResponse;
import io.github.gugomesx10.meets.dto.attendance.ReviewAttendanceRequest;
import io.github.gugomesx10.meets.service.AttendanceReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceReviewController {

    private final AttendanceReviewService attendanceReviewService;

    @PostMapping("/{attendanceDecisionId}/reviews")
    public ResponseEntity<AttendanceReviewResponse> review(
            @PathVariable UUID attendanceDecisionId,
            @Valid @RequestBody ReviewAttendanceRequest request
    ) {

        var review = attendanceReviewService.review(
                attendanceDecisionId,
                request.reviewerId(),
                request.newStatus(),
                request.reason()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AttendanceReviewResponse.from(review));
    }

    @GetMapping("/{attendanceDecisionId}/reviews")
    public ResponseEntity<List<AttendanceReviewResponse>> findHistory(
            @PathVariable UUID attendanceDecisionId
    ) {

        var reviews = attendanceReviewService
                .findHistory(attendanceDecisionId)
                .stream()
                .map(AttendanceReviewResponse::from)
                .toList();

        return ResponseEntity.ok(reviews);
    }
}