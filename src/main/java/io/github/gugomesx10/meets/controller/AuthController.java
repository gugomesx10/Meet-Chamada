package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.auth.CurrentUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(
            @AuthenticationPrincipal OidcUser user
    ) {

        CurrentUserResponse response =
                new CurrentUserResponse(
                        user.getSubject(),
                        user.getFullName(),
                        user.getEmail()
                );

        return ResponseEntity.ok(response);
    }
}