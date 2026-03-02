package com.badarak.application.service;

import com.badarak.domain.exception.ErrorCode;
import com.badarak.domain.exception.InvalidUserNameException;
import com.badarak.domain.exception.UserNotFoundException;
import com.badarak.domain.model.*;
import com.badarak.domain.port.in.UpdateUserUseCase.UpdateUserCommand;
import com.badarak.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UpdateUserServiceTest {

    private UserRepository repository;
    private UpdateUserService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        service = new UpdateUserService(repository);
    }

    private static User sampleUser(UserId id) {
        return User.reconstitute(id,
                new Email("john@example.com"),
                new UserName("John", "Doe"),
                UserStatus.ACTIVE,
                Instant.now(), Instant.now());
    }

    private static UpdateUserCommand command(UserId id, String first, String last) {
        return new UpdateUserCommand(id, first, last);
    }

    @Test
    void should_update_user_name_when_command_is_valid() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        when(repository.findById(id)).thenReturn(of(user));

        service.execute(command(id, "Bob", "Blabla"));

        assertThat(user.getName().firstName()).isEqualTo("Bob");
        assertThat(user.getName().lastName()).isEqualTo("Blabla");
    }

    @Test
    void should_save_user_after_update() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        when(repository.findById(id)).thenReturn(of(user));

        service.execute(command(id, "Bob", "Blabla"));

        verify(repository).save(user);
    }

    @Test
    void should_preserve_user_id_after_update() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        when(repository.findById(id)).thenReturn(of(user));

        service.execute(command(id, "Bob", "Blabla"));

        assertThat(user.getId()).isEqualTo(id);
    }

    @Test
    void should_preserve_email_after_update() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        when(repository.findById(id)).thenReturn(of(user));

        service.execute(command(id, "Bob", "Blabla"));

        assertThat(user.getEmail().value()).isEqualTo("john@example.com");
    }

    @Test
    void should_preserve_status_after_update() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        when(repository.findById(id)).thenReturn(of(user));

        service.execute(command(id, "Bob", "Blabla"));

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void should_allow_update_on_INACTIVE_user() {
        final var id = UserId.generate();
        final var user = User.reconstitute(id, new Email("test@x.com"),
                new UserName("A", "B"), UserStatus.INACTIVE, Instant.now(), Instant.now());
        when(repository.findById(id)).thenReturn(of(user));

        assertThatNoException()
                .isThrownBy(() -> service.execute(command(id, "New", "Name")));

        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void should_strip_whitespace_from_names() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        when(repository.findById(id)).thenReturn(of(user));

        service.execute(command(id, "  Bob  ", "  Blabla  "));

        assertThat(user.getName().firstName()).isEqualTo("Bob");
        assertThat(user.getName().lastName()).isEqualTo("Blabla");
    }

    @Test
    void should_accept_unicode_characters() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        when(repository.findById(id)).thenReturn(of(user));

        assertThatNoException()
                .isThrownBy(() -> service.execute(command(id, "Ézéchiel", "Müller")));

        assertThat(user.getName().firstName()).isEqualTo("Ézéchiel");
    }

    @Test
    void should_accept_names_with_hyphens() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        when(repository.findById(id)).thenReturn(of(user));

        assertThatNoException()
                .isThrownBy(() -> service.execute(command(id, "Jean-Pierre", "Dupont-Martin")));
    }

    @Test
    void should_throw_UserNotFoundException_when_user_not_found() {
        final var id = UserId.generate();
        when(repository.findById(id)).thenReturn(Optional.empty());

        final UpdateUserCommand command = command(id, "Bob", "Blabla");
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void should_include_errorCode_USER_NOT_FOUND_in_exception() {
        final var id = UserId.generate();
        when(repository.findById(id)).thenReturn(Optional.empty());

        final UpdateUserCommand command = command(id, "Bob", "Blabla");
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(UserNotFoundException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND.name());
    }

    @Test
    void should_throw_IllegalArgumentException_when_firstName_blank() {
        final var id = UserId.generate();
        assertThatThrownBy(() -> command(id, "   ", "Blabla"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_throw_IllegalArgumentException_when_lastName_blank() {
        final var id = UserId.generate();
        assertThatThrownBy(() -> command(id, "Bob", "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_throw_InvalidUserNameException_when_firstName_exceeds_100_chars() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        final var tooLong = "A".repeat(101);
        when(repository.findById(id)).thenReturn(of(user));

        final UpdateUserCommand command = command(id, tooLong, "Blabla");
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(InvalidUserNameException.class);
    }

    @Test
    void should_throw_NullPointerException_when_command_is_null() {
        assertThatNullPointerException()
                .isThrownBy(() -> service.execute(null))
                .withMessageContaining("command must not be null");
    }

    @Test
    void should_not_save_when_user_not_found() {
        final var id = UserId.generate();
        when(repository.findById(id)).thenReturn(Optional.empty());

        catchThrowable(() -> service.execute(command(id, "Bob", "Blabla")));

        verify(repository, never()).save(any());
    }

    @Test
    void should_not_save_when_name_validation_fails() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        when(repository.findById(id)).thenReturn(of(user));

        catchThrowable(() -> service.execute(command(id, "   ", "Blabla")));

        verify(repository, never()).save(any());
    }

    @Test
    void should_call_repository_findById_exactly_once() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        when(repository.findById(id)).thenReturn(of(user));

        service.execute(command(id, "Bob", "Blabla"));

        verify(repository, times(1)).findById(id);
    }

    @Test
    void should_call_repository_save_exactly_once() {
        final var id = UserId.generate();
        final var user = sampleUser(id);
        when(repository.findById(id)).thenReturn(of(user));

        service.execute(command(id, "Bob", "Blabla"));

        verify(repository, times(1)).save(user);
    }
}