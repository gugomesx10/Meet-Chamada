package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.institution.CreateInstitutionRequest;
import io.github.gugomesx10.meets.dto.institution.InstitutionResponse;
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

    @PostMapping
    public ResponseEntity<InstitutionResponse> create(
            @Valid @RequestBody CreateInstitutionRequest request
    ) {

        var institution =
                institutionService.create(
                        request.name()
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

        return ResponseEntity.ok(
                InstitutionResponse.from(
                        institutionService.findById(
                                institutionId
                        )
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<InstitutionResponse>> findAll() {

        var institutions =
                institutionService
                        .findAll()
                        .stream()
                        .map(InstitutionResponse::from)
                        .toList();

        return ResponseEntity.ok(institutions);
    }
}