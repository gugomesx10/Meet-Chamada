package io.github.gugomesx10.meets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@Profile("dev")
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        PathPatternRequestMatcher.Builder paths =
                PathPatternRequestMatcher.withDefaults();

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/check-ins"
                                ),
                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/check-ins/{checkInId}/responses"
                                ),
                                paths.matcher(
                                        HttpMethod.PATCH,
                                        "/api/v1/check-ins/{checkInId}/close"
                                ),
                                paths.matcher(
                                        HttpMethod.PATCH,
                                        "/api/v1/check-ins/{checkInId}/cancel"
                                )
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.GET,
                                "/actuator/health"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/check-ins"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/check-ins/{checkInId}/responses"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/check-ins/{checkInId}/close"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/check-ins/{checkInId}/cancel"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/check-ins/{checkInId}/responses"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/check-ins/sessions/{classSessionId}"
                        ).authenticated()

                        .anyRequest().denyAll()
                )

                .httpBasic(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                );

        return http.build();
    }
}