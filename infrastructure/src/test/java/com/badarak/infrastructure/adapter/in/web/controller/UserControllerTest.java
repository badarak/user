package com.badarak.infrastructure.adapter.in.web.controller;

import com.badarak.domain.exception.UserAlreadyExistsException;
import com.badarak.domain.model.Email;
import com.badarak.domain.model.UserId;
import com.badarak.domain.port.in.CreateUserUseCase;
import com.badarak.infrastructure.adapter.in.web.mapper.UserRequestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.badarak.domain.exception.ErrorCode.USER_ALREADY_EXISTS;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@DisplayName("UserController — POST /api/v1/users")
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    CreateUserUseCase useCase;
    @MockitoBean
    UserRequestMapper mapper;

    @Test
    void should_return_201_with_header_location_and_id_in_the_body() throws Exception {
        final var id = UserId.generate();
        when(useCase.execute(any())).thenReturn(id);
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
        when(useCase.execute(any()))
                .thenThrow(new UserAlreadyExistsException(new Email("john@example.com")));
        when(mapper.toCommand(any())).thenCallRealMethod();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("User Already Exists"))
                .andExpect(jsonPath("$.errorCode").value(USER_ALREADY_EXISTS.name()));
    }

    private String validBody() throws Exception {
        return objectMapper.writeValueAsString(
                new java.util.LinkedHashMap<>() {{
                    put("email", "john@example.com");
                    put("firstName", "John");
                    put("lastName", "Doe");
                }}
        );
    }
}