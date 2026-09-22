package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.membership.CreateInstitutionMembershipRequest;
import io.github.gugomesx10.meets.dto.membership.InstitutionMembershipResponse;
import io.github.gugomesx10.meets.dto.membership.UpdateInstitutionMembershipRoleRequest;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.service.AuthenticatedUserService;
import io.github.gugomesx10.meets.service.InstitutionMembershipService;
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
public class InstitutionMembershipController {

    private final InstitutionMembershipService institutionMembershipService;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/{institutionId}/memberships")
    public ResponseEntity<InstitutionMembershipResponse> create(
            @PathVariable UUID institutionId,
            @Valid @RequestBody CreateInstitutionMembershipRequest request
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var membership =
                institutionMembershipService.create(
                        institutionId,
                        request.userId(),
                        request.role(),
                        currentUser
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        InstitutionMembershipResponse.from(
                                membership
                        )
                );
    }

    @PatchMapping("/{institutionId}/memberships/{userId}/role")
    public ResponseEntity<InstitutionMembershipResponse> updateRole(
            @PathVariable UUID institutionId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateInstitutionMembershipRoleRequest request
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var membership =
                institutionMembershipService.updateRole(
                        institutionId,
                        userId,
                        request.role(),
                        currentUser
                );

        return ResponseEntity.ok(
                InstitutionMembershipResponse.from(
                        membership
                )
        );
    }

    @GetMapping("/{institutionId}/memberships")
    public ResponseEntity<List<InstitutionMembershipResponse>> findByInstitution(
            @PathVariable UUID institutionId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var memberships =
                institutionMembershipService
                        .findByInstitution(
                                institutionId,
                                currentUser
                        )
                        .stream()
                        .map(InstitutionMembershipResponse::from)
                        .toList();

        return ResponseEntity.ok(
                memberships
        );
    }

    @GetMapping("/{institutionId}/memberships/{userId}")
    public ResponseEntity<InstitutionMembershipResponse> find(
            @PathVariable UUID institutionId,
            @PathVariable UUID userId
    ) {

        User currentUser =
                authenticatedUserService.getCurrentUser();

        var membership =
                institutionMembershipService.find(
                        institutionId,
                        userId,
                        currentUser
                );

        return ResponseEntity.ok(
                InstitutionMembershipResponse.from(
                        membership
                )
        );
    }
}