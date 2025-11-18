package com.badarak.domain.service;

import com.badarak.domain.exception.ResourceNotFoundException;
import com.badarak.domain.model.User;
import com.badarak.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class UserServiceTest {

    private final UserRepositoryPort repository = mock(UserRepositoryPort.class);
    private final UserService userService = new UserService(repository);


    @Test
    void should_get_user() {
        //Given
        UUID uuid = UUID.randomUUID();
        User user = new User(uuid, "test", "test@gmail.com");
        when(repository.findById(any())).thenReturn(of(user));

        //When
        User actual = userService.findById(uuid);

        //Then
        assertEquals("test", actual.getName());
        assertEquals("test@gmail.com", actual.getEmail());
        Assertions.assertNotNull(actual.getId());
    }

    @Test
    void should_throw_exception_when_user_not_found() {
        //Given
        UUID uuid = UUID.randomUUID();
        when(repository.findById(uuid)).thenReturn(Optional.empty());

        //When && Then
        assertThrows(ResourceNotFoundException.class, () -> userService.findById(uuid));
    }

    @Test
    void should_create_user() {
        //Given
        User user = new User(null, "test", "test@gmail.com");

        //When & Then
        userService.createUser(user);


        verify(repository, times(1)).save(user);
    }

    @Test
    void should_delete_user() {
        //Given
        UUID id = UUID.randomUUID();
        //When
        userService.deleteUser(id);

        verify(repository, times(1)).deleteById(id);
    }
}