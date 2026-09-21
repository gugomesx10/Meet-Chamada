package io.github.gugomesx10.meets.config;

import io.github.gugomesx10.meets.security.CustomOidcUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("oauth")
@RequiredArgsConstructor
public class OAuthSecurityConfig {

    private final CustomOidcUserService customOidcUserService;

    @Bean
    SecurityFilterChain oauthSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.GET,
                                "/actuator/health"
                        ).permitAll()

                        .requestMatchers(
                                "/oauth2/authorization/google",
                                "/login/oauth2/code/google",
                                "/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/auth/me"
                        ).authenticated()

                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo ->
                                userInfo.oidcUserService(
                                        customOidcUserService
                                )
                        )
                        .defaultSuccessUrl(
                                "/api/v1/auth/me",
                                true
                        )
                )

                .oauth2Client(
                        Customizer.withDefaults()
                );

        return http.build();
    }
}