package com.badarak.domain.service;

import com.badarak.domain.exception.ResourceNotFoundException;
import com.badarak.domain.model.User;
import com.badarak.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.PageRequest.of;


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
        //When
        userService.createUser("test", "test@gmail.com");

        //Then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository, times(1)).save(captor.capture());
        assertEquals("test", captor.getValue().getName());
        assertEquals("test@gmail.com", captor.getValue().getEmail());
        assertNotNull(captor.getValue().getId());
    }

    @Test
    void should_delete_user() {
        //Given
        UUID id = UUID.randomUUID();
        //When
        userService.deleteUser(id);

        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void should_update_user() {
        //Given
        UUID id = fromString("13d6c8b9-785d-44b1-aded-320e3eed4d81");
        User user = new User(id, "test", "test@gmail.com");
        when(repository.findById(id)).thenReturn(of(user));

        //When
        userService.updateUser(id, "new_name", "new_email@gmail.com");

        //Then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository, times(1)).save(captor.capture());
        assertEquals("new_name", captor.getValue().getName());
        assertEquals("new_email@gmail.com", captor.getValue().getEmail());
        assertEquals(id, captor.getValue().getId());
    }

    @Test
    void should_get_all_users() {
        //Given
        Page<User> user = new PageImpl<>(
                List.of(new User(UUID.randomUUID(), "test", "test@gmail.com")),
                of(0, 10),
                1
        );
        when(repository.findAll(of(0, 10))).thenReturn(user);

        //When
        Page<User> actual = userService.listUsers(of(0, 10));

        //Then
        assertEquals(1, actual.getTotalElements());
        assertEquals("test", actual.getContent().getFirst().getName());
    }
}