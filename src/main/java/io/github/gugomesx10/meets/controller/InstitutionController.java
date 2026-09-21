package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.institution.CreateInstitutionRequest;
import io.github.gugomesx10.meets.dto.institution.InstitutionResponse;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.service.AuthenticatedUserService;
import io.github.gugomesx10.meets.service.InstitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping
    public ResponseEntity<InstitutionResponse> create(
            @Valid @RequestBody CreateInstitutionRequest request
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var institution =
                institutionService.create(
                        request.name(),
                        currentUser
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        InstitutionResponse.from(
                                institution
                        )
                );
    }

    @GetMapping("/{institutionId}")
    public ResponseEntity<InstitutionResponse> findById(
            @PathVariable UUID institutionId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        return ResponseEntity.ok(
                InstitutionResponse.from(
                        institutionService.findById(
                                institutionId,
                                currentUser
                        )
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<InstitutionResponse>> findAll() {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var institutions =
                institutionService
                        .findAllByUser(currentUser)
                        .stream()
                        .map(InstitutionResponse::from)
                        .toList();

        return ResponseEntity.ok(
                institutions
        );
    }
}