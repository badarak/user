package com.badarak.infrastructure.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestChannelBinderConfiguration.class, JwtTestProperties.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
@DisplayName("API docs — disabled outside the dev profile")
class ApiDocsDisabledByDefaultIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void should_not_expose_api_docs_without_token() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_not_expose_swagger_ui_without_token() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_deny_api_docs_even_with_all_scopes() throws Exception {
        mockMvc.perform(get("/v3/api-docs")
                        .header(AUTHORIZATION, "Bearer " + JwtTestFactory.validToken()))
                .andExpect(status().isForbidden());
    }
}
