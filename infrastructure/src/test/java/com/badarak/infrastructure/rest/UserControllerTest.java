package com.badarak.infrastructure.rest;

import com.badarak.domain.exception.ResourceNotFoundException;
import com.badarak.domain.model.User;
import com.badarak.domain.port.in.UserServicePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
class UserControllerTest {
    @MockitoBean
    private UserServicePort userService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_return_error_when_user_not_found() throws Exception {
        //Given
        UUID uuid = UUID.randomUUID();
        when(userService.findById(uuid)).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(get("/api/users/{id}", uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_return_user() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        when(userService.findById(uuid)).thenReturn(new User(uuid, "test", "test@gmail.com"));

        // When && Then
        mockMvc.perform(get("/api/users/{id}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }

    @Test
    void should_save_user() throws Exception {
        //Given
        CreateOrUpdateUserRequest request = new CreateOrUpdateUserRequest("test", "test@gmail.com");
        when(userService.createUser(anyString(), anyString())).thenReturn(new User(UUID.randomUUID(), "test", "test@gmail.com"));

        //When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
        when(userService.findById(id)).thenReturn(new User(id, "name", "test@gmail.com"));
        when(userService.updateUser(id, "new_name", "new_test@gmail.com")).thenReturn(new User(id, "new_name", "new_test@gmail.com"));

        CreateOrUpdateUserRequest request = new CreateOrUpdateUserRequest(
                "new_name",
                "new_test@gmail.com"
        );

        // When && Then
        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void should_find_all_users() throws Exception {
        //Given
        User user = new User(UUID.randomUUID(), "test", "test@gmail.com");
        Page<User> users = new PageImpl<>(
                List.of(user),
                of(0, 10),
                1);

        when(userService.listUsers(Mockito.any(Pageable.class))).thenReturn(users);
        //When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("test"));
    }
}