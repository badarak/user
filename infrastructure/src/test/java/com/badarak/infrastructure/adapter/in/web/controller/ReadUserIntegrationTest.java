package com.badarak.infrastructure.adapter.in.web.controller;


import com.badarak.domain.model.Email;
import com.badarak.domain.model.User;
import com.badarak.domain.model.UserName;
import com.badarak.domain.port.out.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static com.badarak.domain.exception.ErrorCode.USER_NOT_FOUND;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@DisplayName("READ feature — integration test")
public class ReadUserIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper json;
    @Autowired
    UserRepository repository;

    private User persistActive(String email, String first, String last) {
        final var user = User.create(new Email(email), new UserName(first, last));
        repository.save(user);
        return user;
    }

    private User persistInactive(String email) {
        final var user = User.create(new Email(email), new UserName("Old", "User"));
        user.deactivate();
        repository.save(user);
        return user;
    }

    private UUID createViaApi(String email, String first, String last) throws Exception {
        final var body = json.writeValueAsString(
                Map.of("email", email, "firstName", first, "lastName", last));

        final var location = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        Assertions.assertNotNull(location);
        return UUID.fromString(location.replaceAll(".*/", ""));
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id}")
    class GetById {

        @Test
        void should_return_200_with_full_user_body_when_user_exists() throws Exception {
            final var id = createViaApi("bob@example.com", "Bob", "Blabla");

            mockMvc.perform(get("/api/v1/users/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.email").value("bob@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Bob"))
                    .andExpect(jsonPath("$.lastName").value("Blabla"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void should_return_correct_id_in_response_body() throws Exception {
            final var id = createViaApi("bob@example.com", "Bob", "Blabla");

            mockMvc.perform(get("/api/v1/users/{id}", id))
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        void should_return_INACTIVE_status_for_deactivated_user() throws Exception {
            final var user = persistInactive("deactivated@example.com");

            mockMvc.perform(get("/api/v1/users/{id}", user.getId().value()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("INACTIVE"));
        }

        @Test
        void should_return_404_when_user_does_not_exist() throws Exception {
            mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void should_return_problem_json_with_errorCode_USER_NOT_FOUND_on_404() throws Exception {
            mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(USER_NOT_FOUND.name()))
                    .andExpect(jsonPath("$.title").value("User Not Found"));
        }

        @Test
        void should_return_400_when_id_is_not_a_valid_UUID() throws Exception {
            mockMvc.perform(get("/api/v1/users/not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_same_data_stored_via_repository_directly() throws Exception {
            final var user = persistActive("direct@example.com", "Direct", "Store");

            mockMvc.perform(get("/api/v1/users/{id}", user.getId().value()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("direct@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Direct"))
                    .andExpect(jsonPath("$.lastName").value("Store"));
        }
    }

    @Nested @DisplayName("GET /api/v1/users")
    class ListUsers {

        @Test
        void should_return_200_with_empty_page_when_no_users_exist() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0));
        }

        @Test
        void should_return_all_users_on_default_page() throws Exception {
            createViaApi("a@example.com", "A", "A");
            createViaApi("b@example.com", "B", "B");
            createViaApi("c@example.com", "C", "C");

            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.content", hasSize(3)));
        }

        @Test
        void should_respect_page_size_param() throws Exception {
            createViaApi("a@example.com", "A", "A");
            createViaApi("b@example.com", "B", "B");
            createViaApi("c@example.com", "C", "C");

            mockMvc.perform(get("/api/v1/users").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }

        @Test
        void should_return_correct_second_page() throws Exception {
            createViaApi("a@example.com", "A", "A");
            createViaApi("b@example.com", "B", "B");
            createViaApi("c@example.com", "C", "C");

            mockMvc.perform(get("/api/v1/users").param("page", "1").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.page").value(1));
        }

        @Test
        void should_filter_only_ACTIVE_users_when_status_ACTIVE_requested() throws Exception {
            createViaApi("active@example.com", "Active", "User");
            persistInactive("inactive@example.com");

            mockMvc.perform(get("/api/v1/users").param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
        }

        @Test
        void should_filter_only_INACTIVE_users_when_status_INACTIVE_requested() throws Exception {
            createViaApi("active@example.com", "Active", "User");
            persistInactive("inactive@example.com");

            mockMvc.perform(get("/api/v1/users").param("status", "INACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("INACTIVE"));
        }

        @Test
        void should_include_both_statuses_when_no_filter_applied() throws Exception {
            createViaApi("active@example.com", "Active", "User");
            persistInactive("inactive@example.com");

            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[*].status",
                            containsInAnyOrder("ACTIVE", "INACTIVE")));
        }

        @Test
        void should_return_pagination_metadata_in_response() throws Exception {
            createViaApi("a@example.com", "A", "A");

            mockMvc.perform(get("/api/v1/users").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.totalPages").isNumber());
        }

        @Test
        void should_return_each_user_with_all_required_fields() throws Exception {
            createViaApi("full@example.com", "Full", "Fields");

            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").exists())
                    .andExpect(jsonPath("$.content[0].email").exists())
                    .andExpect(jsonPath("$.content[0].firstName").exists())
                    .andExpect(jsonPath("$.content[0].lastName").exists())
                    .andExpect(jsonPath("$.content[0].status").exists())
                    .andExpect(jsonPath("$.content[0].createdAt").exists())
                    .andExpect(jsonPath("$.content[0].updatedAt").exists());
        }

        @Test
        void should_return_400_when_size_exceeds_100() throws Exception {
            mockMvc.perform(get("/api/v1/users").param("size", "101"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_when_page_is_negative() throws Exception {
            mockMvc.perform(get("/api/v1/users").param("page", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_for_invalid_status_value() throws Exception {
            mockMvc.perform(get("/api/v1/users").param("status", "UNKNOWN"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_reflect_user_created_via_POST_in_list() throws Exception {
            final var id = createViaApi("listed@example.com", "Listed", "User");

            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].id", hasItem(id.toString())));
        }
    }
}
