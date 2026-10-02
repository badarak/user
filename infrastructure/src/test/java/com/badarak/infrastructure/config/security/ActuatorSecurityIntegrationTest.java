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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestChannelBinderConfiguration.class, JwtTestProperties.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
@DisplayName("Actuator — exposure and access")
class ActuatorSecurityIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void should_expose_health_without_token_and_without_details() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist())
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void should_expose_info_without_token() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    void should_return_401_for_other_actuator_endpoints_without_token() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return_403_for_other_actuator_endpoints_even_with_all_scopes() throws Exception {
        mockMvc.perform(get("/actuator/env")
                        .header(AUTHORIZATION, "Bearer " + JwtTestFactory.validToken()))
                .andExpect(status().isForbidden());
    }
}
