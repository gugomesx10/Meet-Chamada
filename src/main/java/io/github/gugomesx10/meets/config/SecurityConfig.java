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
                                ),
                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/attendance/sessions/{classSessionId}/students/{studentId}/evaluate"
                                ),
                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/attendance/sessions/{classSessionId}/evaluate"
                                ),
                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/attendance/{attendanceDecisionId}/reviews"
                                ),
                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/presence-evidence/sessions/{classSessionId}/teacher-confirmations"
                                ),
                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/class-sessions"
                                ),
                                paths.matcher(
                                        HttpMethod.PATCH,
                                        "/api/v1/class-sessions/{classSessionId}/start"
                                ),
                                paths.matcher(
                                        HttpMethod.PATCH,
                                        "/api/v1/class-sessions/{classSessionId}/complete"
                                ),
                                paths.matcher(
                                        HttpMethod.PATCH,
                                        "/api/v1/class-sessions/{classSessionId}/cancel"
                                ),
                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/session-blocks/sessions/{classSessionId}"
                                ),
                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/users"
                                ),
                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/institutions"
                                ),
                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/institutions/{institutionId}/memberships"
                                ),

                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/courses"
                                ),

                                paths.matcher(
                                        HttpMethod.PATCH,
                                        "/api/v1/courses/{courseId}/activate"
                                ),

                                paths.matcher(
                                        HttpMethod.PATCH,
                                        "/api/v1/courses/{courseId}/complete"
                                ),

                                paths.matcher(
                                        HttpMethod.PATCH,
                                        "/api/v1/courses/{courseId}/cancel"
                                ),

                                paths.matcher(
                                        HttpMethod.POST,
                                        "/api/v1/courses/{courseId}/memberships"
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

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/attendance/sessions/{classSessionId}/students/{studentId}/evaluate"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/attendance/sessions/{classSessionId}/evaluate"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/attendance/sessions/{classSessionId}/students/{studentId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/attendance/sessions/{classSessionId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/attendance/sessions/{classSessionId}/review-required"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/attendance/{attendanceDecisionId}/reviews"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/attendance/{attendanceDecisionId}/reviews"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/presence-evidence/sessions/{classSessionId}/teacher-confirmations"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/presence-evidence/sessions/{classSessionId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/presence-evidence/sessions/{classSessionId}/students/{studentId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/presence-evidence/sessions/{classSessionId}/students/{studentId}/types/{type}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/presence-evidence/blocks/{sessionBlockId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/class-sessions"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/class-sessions/{classSessionId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/class-sessions/courses/{courseId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/class-sessions/{classSessionId}/start"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/class-sessions/{classSessionId}/complete"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/class-sessions/{classSessionId}/cancel"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/session-blocks/sessions/{classSessionId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/session-blocks/{sessionBlockId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/session-blocks/sessions/{classSessionId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/session-blocks/instructors/{instructorId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/users"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/users"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/users/{userId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/institutions"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/institutions"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/institutions/{institutionId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/institutions/{institutionId}/memberships"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/institutions/{institutionId}/memberships"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/institutions/{institutionId}/memberships/{userId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/courses"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/courses/{courseId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/courses/institutions/{institutionId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/courses/{courseId}/activate"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/courses/{courseId}/complete"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/courses/{courseId}/cancel"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/courses/{courseId}/memberships"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/courses/{courseId}/memberships"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/courses/{courseId}/memberships/{userId}"
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