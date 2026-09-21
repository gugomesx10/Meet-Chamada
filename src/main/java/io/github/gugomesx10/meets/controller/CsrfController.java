package io.github.gugomesx10.meets.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Profile("oauth")
public class CsrfController {

    @GetMapping("/csrf")
    public CsrfToken csrf(
            CsrfToken csrfToken
    ) {
        return csrfToken;
    }
}