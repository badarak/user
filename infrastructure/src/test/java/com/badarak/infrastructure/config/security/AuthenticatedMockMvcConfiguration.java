package com.badarak.infrastructure.config.security;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Sends a valid bearer token with every MockMvc request, so functional tests stay focused on business behavior.
 * Security behavior itself is covered by dedicated tests.
 */
@TestConfiguration(proxyBeanMethods = false)
@Import(JwtTestProperties.class)
public class AuthenticatedMockMvcConfiguration {

    @Bean
    MockMvcBuilderCustomizer bearerTokenByDefault() {
        return builder -> builder.defaultRequest(
                get("/").header(AUTHORIZATION, "Bearer " + JwtTestFactory.validToken()));
    }
}
