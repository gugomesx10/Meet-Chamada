package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.auth.CurrentUserResponse;
import io.github.gugomesx10.meets.entity.User;
import io.github.gugomesx10.meets.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticatedUserService authenticatedUserService;

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me() {

        User user = authenticatedUserService.getCurrentUser();

        return ResponseEntity.ok(
                new CurrentUserResponse(
                        user.getId(),
                        user.getExternalId(),
                        user.getName(),
                        user.getEmail()
                )
        );
    }
}