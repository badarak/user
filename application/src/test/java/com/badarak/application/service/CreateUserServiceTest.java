package com.badarak.application.service;

import com.badarak.domain.event.UserCreatedEvent;
import com.badarak.domain.exception.UserAlreadyExistsException;
import com.badarak.domain.model.Email;
import com.badarak.domain.model.User;
import com.badarak.domain.model.UserId;
import com.badarak.domain.port.in.CreateUserUseCase.CreateUserCommand;
import com.badarak.domain.port.out.DomainEventPublisher;
import com.badarak.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateUserServiceTest {

    private UserRepository userRepository;
    private DomainEventPublisher eventPublisher;
    private CreateUserService service;


    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        eventPublisher = mock(DomainEventPublisher.class);
        service = new CreateUserService(userRepository, eventPublisher);
    }

    @Test
    void should_return_the_generated_userId() {
        givenEmailDoesNotExist();
        final var command = validCommand();

        final var result = service.execute(command);

        assertThat(result).isNotNull().isInstanceOf(UserId.class);
    }

    @Test
    void should_save_user_to_repository() {
        givenEmailDoesNotExist();
        final var captor = ArgumentCaptor.forClass(User.class);

        service.execute(validCommand());

        verify(userRepository).save(captor.capture());
        final var savedUser = captor.getValue();
        assertThat(savedUser.getEmail().value()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getName().firstName()).isEqualTo("John");
        assertThat(savedUser.getName().lastName()).isEqualTo("Doe");
    }

    @Test
    void should_publish_userCreatedEvent() {
        givenEmailDoesNotExist();

        service.execute(validCommand());

        verify(eventPublisher, atLeastOnce()).publish(any(UserCreatedEvent.class));
    }

    @Test
    void should_throws_UserAlreadyExistsException_when_user_already_exists() {
        givenEmailExists();

        assertThatThrownBy(() -> service.execute(validCommand()))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void should_never_saves_user_when_user_already_exists() {
        givenEmailExists();

        catchThrowable(() -> service.execute(validCommand()));

        verify(userRepository, never()).save(any());
    }

    @Test
    void should_never_never_publish_event_when_user_already_exists() {
        givenEmailExists();

        catchThrowable(() -> service.execute(validCommand()));

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void should_throws_NullPointerException_when_command_is_null() {
        assertThatNullPointerException()
                .isThrownBy(() -> service.execute(null));
    }

    private static CreateUserCommand validCommand() {
        return new CreateUserCommand("john.doe@example.com", "John", "Doe");
    }

    private void givenEmailDoesNotExist() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    }

    private void givenEmailExists() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);
    }
}