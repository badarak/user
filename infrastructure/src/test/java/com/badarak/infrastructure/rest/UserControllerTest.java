package com.badarak.infrastructure.rest;

import com.badarak.domain.model.User;
import com.badarak.domain.port.out.UserRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
class UserControllerTest {
    @MockitoBean
    private UserRepositoryPort repository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_return_error_when_user_not_found() throws Exception {
        //Given
        UUID uuid = UUID.randomUUID();
        Mockito.when(repository.findById(uuid)).thenReturn(empty());

        mockMvc.perform(get("/api/users/{id}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_return_user() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        Mockito.when(repository.findById(uuid)).thenReturn(of(new User(uuid, "test", "test@gmail.com")));

        // When && Then
        mockMvc.perform(get("/api/users/{id}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }

    @Test
    void should_save_user() throws Exception {
        //Given
        User user = new User(null, "test", "test@gmail.com");

        //When
        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());
    }

    @Test
    void should_delete_user() throws Exception {
        //Given
        UUID id = UUID.randomUUID();

        //When
        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_update_user() throws Exception {
        //Given
        UUID id = fromString("8a8bd2cc-caa6-4fb3-9ad4-2244393cb608");
        Mockito.when(repository.findById(id)).thenReturn(of(new User(id, "name", "test@gmail.com")));

        User user = new User(
                id,
                "new_name",
                "new_test@gmail.com"
        );

        // When && Then
        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }
}