package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.attendance.AttendanceDecisionResponse;
import io.github.gugomesx10.meets.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping(
            "/sessions/{classSessionId}/students/{studentId}/evaluate"
    )
    public ResponseEntity<AttendanceDecisionResponse> evaluateStudent(
            @PathVariable UUID classSessionId,
            @PathVariable UUID studentId
    ) {

        var decision = attendanceService.evaluate(
                studentId,
                classSessionId
        );

        return ResponseEntity.ok(
                AttendanceDecisionResponse.from(decision)
        );
    }

    @PostMapping("/sessions/{classSessionId}/evaluate")
    public ResponseEntity<List<AttendanceDecisionResponse>> evaluateSession(
            @PathVariable UUID classSessionId
    ) {

        var decisions = attendanceService
                .evaluateSession(classSessionId)
                .stream()
                .map(AttendanceDecisionResponse::from)
                .toList();

        return ResponseEntity.ok(decisions);
    }

    @GetMapping(
            "/sessions/{classSessionId}/students/{studentId}"
    )
    public ResponseEntity<AttendanceDecisionResponse> findStudentAttendance(
            @PathVariable UUID classSessionId,
            @PathVariable UUID studentId
    ) {

        var decision =
                attendanceService.findByStudentAndSession(
                        studentId,
                        classSessionId
                );

        return ResponseEntity.ok(
                AttendanceDecisionResponse.from(decision)
        );
    }

    @GetMapping("/sessions/{classSessionId}")
    public ResponseEntity<List<AttendanceDecisionResponse>> findBySession(
            @PathVariable UUID classSessionId
    ) {

        var decisions = attendanceService
                .findBySession(classSessionId)
                .stream()
                .map(AttendanceDecisionResponse::from)
                .toList();

        return ResponseEntity.ok(decisions);
    }

    @GetMapping(
            "/sessions/{classSessionId}/review-required"
    )
    public ResponseEntity<List<AttendanceDecisionResponse>>
    findReviewRequired(
            @PathVariable UUID classSessionId
    ) {

        var decisions = attendanceService
                .findReviewRequired(classSessionId)
                .stream()
                .map(AttendanceDecisionResponse::from)
                .toList();

        return ResponseEntity.ok(decisions);
    }
}