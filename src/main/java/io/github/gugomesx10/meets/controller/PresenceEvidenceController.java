package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.evidence.PresenceEvidenceResponse;
import io.github.gugomesx10.meets.dto.evidence.TeacherConfirmationRequest;
import io.github.gugomesx10.meets.entity.PresenceEvidence;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.entity.enums.PresenceEvidenceType;
import io.github.gugomesx10.meets.service.AuthenticatedUserService;
import io.github.gugomesx10.meets.service.PresenceEvidenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/presence-evidence")
@RequiredArgsConstructor
public class PresenceEvidenceController {

    private final PresenceEvidenceService presenceEvidenceService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/sessions/{classSessionId}/teacher-confirmations")
    public ResponseEntity<PresenceEvidenceResponse> registerTeacherConfirmation(
            @PathVariable UUID classSessionId,
            @Valid @RequestBody TeacherConfirmationRequest request
    ) {

        User teacher = authenticatedUserService.getCurrentUser();

        PresenceEvidence evidence =
                presenceEvidenceService.registerTeacherConfirmation(
                        request.studentId(),
                        classSessionId,
                        request.sessionBlockId(),
                        teacher,
                        request.details()
                );

        return ResponseEntity.ok(
                PresenceEvidenceResponse.from(evidence)
        );
    }

    @GetMapping(
            "/sessions/{classSessionId}"
    )
    public ResponseEntity<List<PresenceEvidenceResponse>>
    findBySession(
            @PathVariable UUID classSessionId
    ) {

        var evidence = presenceEvidenceService
                .findBySession(classSessionId)
                .stream()
                .map(PresenceEvidenceResponse::from)
                .toList();

        return ResponseEntity.ok(evidence);
    }

    @GetMapping(
            "/sessions/{classSessionId}/students/{studentId}"
    )
    public ResponseEntity<List<PresenceEvidenceResponse>>
    findByStudentAndSession(
            @PathVariable UUID classSessionId,
            @PathVariable UUID studentId
    ) {

        var evidence = presenceEvidenceService
                .findByStudentAndSession(
                        studentId,
                        classSessionId
                )
                .stream()
                .map(PresenceEvidenceResponse::from)
                .toList();

        return ResponseEntity.ok(evidence);
    }

    @GetMapping(
            "/sessions/{classSessionId}/students/{studentId}/types/{type}"
    )
    public ResponseEntity<List<PresenceEvidenceResponse>>
    findByType(
            @PathVariable UUID classSessionId,
            @PathVariable UUID studentId,
            @PathVariable PresenceEvidenceType type
    ) {

        var evidence = presenceEvidenceService
                .findByStudentSessionAndType(
                        studentId,
                        classSessionId,
                        type
                )
                .stream()
                .map(PresenceEvidenceResponse::from)
                .toList();

        return ResponseEntity.ok(evidence);
    }

    @GetMapping(
            "/blocks/{sessionBlockId}"
    )
    public ResponseEntity<List<PresenceEvidenceResponse>>
    findBySessionBlock(
            @PathVariable UUID sessionBlockId
    ) {

        var evidence = presenceEvidenceService
                .findBySessionBlock(sessionBlockId)
                .stream()
                .map(PresenceEvidenceResponse::from)
                .toList();

        return ResponseEntity.ok(evidence);
    }
}