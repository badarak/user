package com.badarak.infrastructure.adapter.in.web.controller;

import com.badarak.domain.exception.UserAlreadyExistsException;
import com.badarak.domain.exception.UserAlreadyInactiveException;
import com.badarak.domain.exception.UserNotFoundException;
import com.badarak.domain.model.*;
import com.badarak.domain.port.in.*;
import com.badarak.infrastructure.adapter.in.web.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.badarak.domain.exception.ErrorCode.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@DisplayName("UserController — CRUD /api/v1/users")
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper json;

    @MockitoBean
    CreateUserUseCase createUser;
    @MockitoBean
    GetUserUseCase getUser;
    @MockitoBean
    ListUsersUseCase listUsers;
    @MockitoBean
    DeleteUserUseCase deleteUser;
    @MockitoBean
    UpdateUserUseCase  updateUser;

    @MockitoBean
    UserMapper mapper;

    private final UUID ID = UUID.randomUUID();
    private final UserId UID = new UserId(ID);

    private User sampleUser() {
        return User.reconstitute(UID,
                new Email("john@example.com"),
                new UserName("John", "Doe"),
                UserStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );
    }

    private String validBody() throws Exception {
        return json.writeValueAsString(
                new java.util.LinkedHashMap<>() {{
                    put("email", "john@example.com");
                    put("firstName", "John");
                    put("lastName", "Doe");
                }}
        );
    }

    @Nested
    @DisplayName("POST /api/v1/users")
    class CreateEndpoint {

        @Test
        void should_return_201_with_header_location_and_id_in_the_body() throws Exception {
            final var id = UserId.generate();
            when(createUser.execute(any())).thenReturn(id);
            when(mapper.toCommand(any())).thenCallRealMethod();
            when(mapper.toResponse(any())).thenCallRealMethod();

            mockMvc.perform(post("/api/v1/users")
                            .contentType(APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/api/v1/users/" + id.value())))
                    .andExpect(jsonPath("$.id").value(id.value().toString()));
        }

        @Test
        void should_return_400_when_email_mission() throws Exception {
            mockMvc.perform(post("/api/v1/users")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"firstName":"John","lastName":"Doe"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[?(@.field=='email')]").exists());
        }

        @Test
        void should_return_400_when_email_format_invalid() throws Exception {
            mockMvc.perform(post("/api/v1/users")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"email":"not-valid","firstName":"John","lastName":"Doe"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_when_first_name_is_blank() throws Exception {
            mockMvc.perform(post("/api/v1/users")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"email":"a@b.com","firstName":"   ","lastName":"Doe"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_when_last_name_is_blank() throws Exception {
            mockMvc.perform(post("/api/v1/users")
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"email":"a@b.com","firstName":"John","lastName":" "}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_409_when_email_already_existing_error_code() throws Exception {
            when(createUser.execute(any()))
                    .thenThrow(new UserAlreadyExistsException(new Email("john@example.com")));
            when(mapper.toCommand(any())).thenCallRealMethod();

            mockMvc.perform(post("/api/v1/users")
                            .contentType(APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("User Already Exists"))
                    .andExpect(jsonPath("$.errorCode").value(USER_ALREADY_EXISTS.name()));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id}")
    class GetEndpoint {

        @Test
        void should_return_200_when_user_found() throws Exception {
            final var user = sampleUser();
            when(getUser.execute(any())).thenReturn(user);
            when(mapper.toUserResponse(user)).thenCallRealMethod();

            mockMvc.perform(get("/api/v1/users/{id}", ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ID.toString()))
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        void should_return_404_when_user_not_found() throws Exception {
            when(getUser.execute(any())).thenThrow(new UserNotFoundException(UID));

            mockMvc.perform(get("/api/v1/users/{id}", ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
        }

        @Test
        void should_return_400_when_id_is_not_a_valid_UUI() throws Exception {
            mockMvc.perform(get("/api/v1/users/not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users")
    class ListEndpoint {

        @Test
        void should_return_200() throws Exception {
            final var user = sampleUser();
            final var page = new UserPage(List.of(user), 0, 20, 1L, 1);
            when(listUsers.execute(any())).thenReturn(page);
            when(mapper.toPageResponse(any())).thenCallRealMethod();
            when(mapper.toUserResponse(any())).thenCallRealMethod();

            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        void should_return_200_and_filtered_by_ACTIVE_status() throws Exception {
            when(listUsers.execute(argThat(q -> q.status() == UserStatus.ACTIVE)))
                    .thenReturn(new UserPage(List.of(), 0, 20, 0L, 0));
            when(mapper.toPageResponse(any())).thenCallRealMethod();

            mockMvc.perform(get("/api/v1/users").param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        void should_return_400_when_invalid_size() throws Exception {
            mockMvc.perform(get("/api/v1/users").param("size", "200"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested @DisplayName("PUT /api/v1/users/{id}")
    class UpdateEndpoint {

        @Test
        void should_return_200() throws Exception {
            final var user = sampleUser();
            when(updateUser.execute(any())).thenReturn(user);
            when(mapper.toUpdateUserCommand(any(), any())).thenCallRealMethod();
            when(mapper.toUserResponse(user)).thenCallRealMethod();

            mockMvc.perform(put("/api/v1/users/{id}", ID)
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"firstName":"Bob","lastName":"Blabla"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ID.toString()));
        }

        @Test
        void should_return_400_when_first_name_missing() throws Exception {
            mockMvc.perform(put("/api/v1/users/{id}", ID)
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"lastName":"Blabla"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_400_when_last_name_missing() throws Exception {
            mockMvc.perform(put("/api/v1/users/{id}", ID)
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"fistName":"Bob"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void should_return_404_when_user_not_found() throws Exception {
            doThrow(new UserNotFoundException(UID)).when(updateUser).execute(any());
            when(mapper.toUpdateUserCommand(any(), any())).thenCallRealMethod();

            mockMvc.perform(put("/api/v1/users/{id}", ID)
                            .contentType(APPLICATION_JSON)
                            .content("""
                                    {"firstName":"NotExist","lastName":"User"}
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id}")
    class DeleteEndpoint {

        @Test
        void should_return_204() throws Exception {
            doNothing().when(deleteUser).execute(any());
            mockMvc.perform(delete("/api/v1/users/{id}", ID))
                    .andExpect(status().isNoContent());
        }

        @Test
        void should_return_404_when_user_not_found() throws Exception {
            doThrow(new UserNotFoundException(UID)).when(deleteUser).execute(any());
            mockMvc.perform(delete("/api/v1/users/{id}", ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(USER_NOT_FOUND.name()));
        }

        @Test
        void should_return_409_when_user_already_inactive() throws Exception {
            doThrow(new UserAlreadyInactiveException(UID)).when(deleteUser).execute(any());
            mockMvc.perform(delete("/api/v1/users/{id}", ID))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value(USER_ALREADY_INACTIVE.name()));
        }
    }
}