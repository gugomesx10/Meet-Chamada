package io.github.gugomesx10.meets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("oauth")
public class OAuthSecurityConfig {

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

                .oauth2Login(oauth ->
                        oauth.defaultSuccessUrl(
                                "/api/v1/auth/me",
                                true
                        )
                );

        return http.build();
    }
}