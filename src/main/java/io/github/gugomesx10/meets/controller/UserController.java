package io.github.gugomesx10.meets.controller;

import io.github.gugomesx10.meets.dto.user.CreateUserRequest;
import io.github.gugomesx10.meets.dto.user.UserResponse;
import io.github.gugomesx10.meets.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest request
    ) {

        var user = userService.create(
                request.name(),
                request.email(),
                request.externalId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.from(user));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> findById(
            @PathVariable UUID userId
    ) {

        return ResponseEntity.ok(
                UserResponse.from(
                        userService.findById(userId)
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {

        var users = userService
                .findAll()
                .stream()
                .map(UserResponse::from)
                .toList();

        return ResponseEntity.ok(users);
    }
}