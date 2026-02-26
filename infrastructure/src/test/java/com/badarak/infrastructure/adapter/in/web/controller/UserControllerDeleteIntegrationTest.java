package com.badarak.infrastructure.adapter.in.web.controller;


import com.badarak.domain.model.*;
import com.badarak.domain.port.out.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.badarak.domain.exception.ErrorCode.USER_ALREADY_INACTIVE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("DELETE feature — integration tests")
class DeleteUserIntegrationTest {

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
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        return UUID.fromString(location.replaceAll(".*/", ""));
    }

    private User fetchFromRepo(UUID id) {
        return repository.findById(new UserId(id))
                .orElseThrow(() -> new AssertionError("User should exist in repository"));
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id}")
    class DeleteUser {

        @Test
        void should_return_204_when_active_user_is_deleted() throws Exception {
            final var id = createViaApi("delete@example.com", "Delete", "Me");

            mockMvc.perform(delete("/api/v1/users/{id}", id))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }

        @Test
        void should_set_status_to_INACTIVE_after_delete() throws Exception {
            final var id = createViaApi("soft@example.com", "Soft", "Delete");

            mockMvc.perform(delete("/api/v1/users/{id}", id))
                    .andExpect(status().isNoContent());

            final var user = fetchFromRepo(id);
            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        }

        @Test
        void should_keep_user_in_database_after_delete() throws Exception {
            final var id = createViaApi("keep@example.com", "Keep", "Me");

            mockMvc.perform(delete("/api/v1/users/{id}", id));

            final var exists = repository.findById(new UserId(id)).isPresent();
            assertThat(exists).isTrue();
        }

        @Test
        void should_preserve_email_and_name_after_delete() throws Exception {
            final var id = createViaApi("preserve@example.com", "Preserve", "Data");

            mockMvc.perform(delete("/api/v1/users/{id}", id));

            final var user = fetchFromRepo(id);
            assertThat(user.getEmail().value()).isEqualTo("preserve@example.com");
            assertThat(user.getName().firstName()).isEqualTo("Preserve");
            assertThat(user.getName().lastName()).isEqualTo("Data");
        }

        @Test
        void should_update_updatedAt_timestamp_after_delete() throws Exception {
            final var user = persistActive("timestamp@example.com", "Time", "Stamp");
            final var originalUpdatedAt = user.getUpdatedAt();

            Thread.sleep(10);

            mockMvc.perform(delete("/api/v1/users/{id}", user.getId().value()));

            final var updated = fetchFromRepo(user.getId().value());
            assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
        }

        @Test
        void should_still_be_retrievable_via_GET_after_delete() throws Exception {
            final var id = createViaApi("still@example.com", "Still", "Here");

            mockMvc.perform(delete("/api/v1/users/{id}", id));

            mockMvc.perform(get("/api/v1/users/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.status").value("INACTIVE"));
        }

        @Test
        void should_appear_in_list_with_status_INACTIVE_after_delete() throws Exception {
            final var id = createViaApi("listed@example.com", "Listed", "Inactive");

            mockMvc.perform(delete("/api/v1/users/{id}", id));

            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].id", hasItem(id.toString())))
                    .andExpect(jsonPath("$.content[?(@.id=='" + id + "')].status").value("INACTIVE"));
        }

        @Test
        void should_appear_in_list_filtered_by_INACTIVE_after_delete() throws Exception {
            final var id = createViaApi("filter@example.com", "Filter", "Test");

            mockMvc.perform(delete("/api/v1/users/{id}", id));

            mockMvc.perform(get("/api/v1/users").param("status", "INACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].id", hasItem(id.toString())));
        }

        @Test
        void should_not_appear_in_list_filtered_by_ACTIVE_after_delete() throws Exception {
            final var id = createViaApi("notactive@example.com", "Not", "Active");

            mockMvc.perform(delete("/api/v1/users/{id}", id));

            mockMvc.perform(get("/api/v1/users").param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].id", not(hasItem(id.toString()))));
        }

        @Test
        void should_be_idempotent_with_409_on_second_delete() throws Exception {
            final var id = createViaApi("twice@example.com", "Twice", "Delete");

            mockMvc.perform(delete("/api/v1/users/{id}", id))
                    .andExpect(status().isNoContent());

            mockMvc.perform(delete("/api/v1/users/{id}", id))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("USER_ALREADY_INACTIVE"));
        }

        @Test
        void should_return_404_when_user_does_not_exist() throws Exception {
            final var randomId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/users/{id}", randomId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"))
                    .andExpect(jsonPath("$.title").value("User Not Found"));
        }

        @Test
        void should_return_404_with_standard_format() throws Exception {
            mockMvc.perform(delete("/api/v1/users/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type").exists())
                    .andExpect(jsonPath("$.title").exists())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.detail").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        void should_return_409_when_user_already_inactive() throws Exception {
            final var user = persistInactive("already@example.com");

            mockMvc.perform(delete("/api/v1/users/{id}", user.getId().value()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value(USER_ALREADY_INACTIVE.name()))
                    .andExpect(jsonPath("$.title").value("User Already Inactive"));
        }

        @Test
        void should_return_409_with_standard_format_for_already_inactive() throws Exception {
            final var user = persistInactive("inactive@example.com");

            mockMvc.perform(delete("/api/v1/users/{id}", user.getId().value()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.type").value(containsString("user-already-inactive")))
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        void should_return_400_when_id_is_not_valid_UUID() throws Exception {
            mockMvc.perform(delete("/api/v1/users/not-a-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value(containsString("invalid-format")));
        }

        @Test
        void should_not_allow_duplicate_email_after_delete() throws Exception {
            final var email = "unique@example.com";
            final var id = createViaApi(email, "First", "User");

            mockMvc.perform(delete("/api/v1/users/{id}", id));

            final var body = json.writeValueAsString(
                    Map.of("email", email, "firstName", "Second", "lastName", "User"));

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("USER_ALREADY_EXISTS"));
        }

        @Test
        void should_handle_multiple_deletes_in_sequence() throws Exception {
            final var id1 = createViaApi("multi1@example.com", "Multi", "One");
            final var id2 = createViaApi("multi2@example.com", "Multi", "Two");
            final var id3 = createViaApi("multi3@example.com", "Multi", "Three");

            mockMvc.perform(delete("/api/v1/users/{id}", id1))
                    .andExpect(status().isNoContent());
            mockMvc.perform(delete("/api/v1/users/{id}", id2))
                    .andExpect(status().isNoContent());
            mockMvc.perform(delete("/api/v1/users/{id}", id3))
                    .andExpect(status().isNoContent());

            assertThat(fetchFromRepo(id1).getStatus()).isEqualTo(UserStatus.INACTIVE);
            assertThat(fetchFromRepo(id2).getStatus()).isEqualTo(UserStatus.INACTIVE);
            assertThat(fetchFromRepo(id3).getStatus()).isEqualTo(UserStatus.INACTIVE);
        }

        @Test
        void should_not_affect_other_users_when_deleting_one() throws Exception {
            final var id1 = createViaApi("keep1@example.com", "Keep", "One");
            final var id2 = createViaApi("delete@example.com", "Delete", "This");
            final var id3 = createViaApi("keep2@example.com", "Keep", "Two");

            mockMvc.perform(delete("/api/v1/users/{id}", id2));

            assertThat(fetchFromRepo(id1).getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(fetchFromRepo(id3).getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }
}
