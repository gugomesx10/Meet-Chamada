package io.github.gugomesx10.meets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("oauth")
public class GoogleMeetClientConfig {

    @Bean
    RestClient googleMeetRestClient(
            OAuth2AuthorizedClientManager authorizedClientManager
    ) {

        OAuth2ClientHttpRequestInterceptor interceptor =
                new OAuth2ClientHttpRequestInterceptor(
                        authorizedClientManager
                );

        return RestClient
                .builder()
                .baseUrl(
                        "https://meet.googleapis.com/v2"
                )
                .requestInterceptor(
                        interceptor
                )
                .build();
    }
}