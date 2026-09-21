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

        User teacher =
                authenticatedUserService.getCurrentUser();

        PresenceEvidence evidence =
                presenceEvidenceService.registerTeacherConfirmation(
                        request.studentId(),
                        classSessionId,
                        request.sessionBlockId(),
                        teacher,
                        request.details()
                );

        return ResponseEntity.ok(
                PresenceEvidenceResponse.from(
                        evidence
                )
        );
    }

    @GetMapping("/sessions/{classSessionId}")
    public ResponseEntity<List<PresenceEvidenceResponse>>
    findBySession(
            @PathVariable UUID classSessionId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var evidence =
                presenceEvidenceService
                        .findBySession(
                                classSessionId,
                                currentUser
                        )
                        .stream()
                        .map(PresenceEvidenceResponse::from)
                        .toList();

        return ResponseEntity.ok(
                evidence
        );
    }

    @GetMapping(
            "/sessions/{classSessionId}/students/{studentId}"
    )
    public ResponseEntity<List<PresenceEvidenceResponse>>
    findByStudentAndSession(
            @PathVariable UUID classSessionId,
            @PathVariable UUID studentId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var evidence =
                presenceEvidenceService
                        .findByStudentAndSession(
                                studentId,
                                classSessionId,
                                currentUser
                        )
                        .stream()
                        .map(PresenceEvidenceResponse::from)
                        .toList();

        return ResponseEntity.ok(
                evidence
        );
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

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var evidence =
                presenceEvidenceService
                        .findByStudentSessionAndType(
                                studentId,
                                classSessionId,
                                type,
                                currentUser
                        )
                        .stream()
                        .map(PresenceEvidenceResponse::from)
                        .toList();

        return ResponseEntity.ok(
                evidence
        );
    }

    @GetMapping("/blocks/{sessionBlockId}")
    public ResponseEntity<List<PresenceEvidenceResponse>>
    findBySessionBlock(
            @PathVariable UUID sessionBlockId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var evidence =
                presenceEvidenceService
                        .findBySessionBlock(
                                sessionBlockId,
                                currentUser
                        )
                        .stream()
                        .map(PresenceEvidenceResponse::from)
                        .toList();

        return ResponseEntity.ok(
                evidence
        );
    }
}