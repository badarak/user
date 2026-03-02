package com.badarak.application.service;

import com.badarak.domain.exception.ErrorCode;
import com.badarak.domain.exception.UserNotFoundException;
import com.badarak.domain.model.*;
import com.badarak.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GetUserServiceTest {
    private GetUserService service;
    private UserRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = mock(UserRepository.class);
        this.service = new GetUserService(repository);
    }

    private static User active(UserId id) {
        return User.reconstitute(id, new Email("john@example.com"),
                new UserName("John", "Doe"), UserStatus.ACTIVE,
                Instant.now(), Instant.now());
    }

    private static User inactive(UserId id) {
        return User.reconstitute(id, new Email("jane@example.com"),
                new UserName("Jane", "Smith"), UserStatus.INACTIVE,
                Instant.now().minusSeconds(3600), Instant.now());
    }

    @Test
    void should_return_user_when_id_exists() {
        final var id = UserId.generate();
        when(repository.findById(id)).thenReturn(of(active(id)));
        assertThat(service.execute(id)).isEqualTo(active(id));
    }

    @Test
    void should_map_all_fields_when_user_is_found() {
        final var id = UserId.generate();
        final var user = active(id);
        when(repository.findById(id)).thenReturn(of(user));
        final var result = service.execute(id);
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getEmail().value()).isEqualTo("john@example.com");
        assertThat(result.getName().firstName()).isEqualTo("John");
        assertThat(result.getName().lastName()).isEqualTo("Doe");
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void should_return_inactive_user_without_altering_its_status() {
        final var id = UserId.generate();
        when(repository.findById(id)).thenReturn(of(inactive(id)));
        assertThat(service.execute(id).getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void should_delegate_to_repository_with_exact_id_instance() {
        final var id = UserId.generate();
        when(repository.findById(id)).thenReturn(of(active(id)));
        service.execute(id);
        verify(repository, times(1)).findById(id);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void should_never_write_to_repository_on_read() {
        final var id = UserId.generate();
        when(repository.findById(id)).thenReturn(of(active(id)));
        service.execute(id);
        verify(repository, never()).save(any());
    }

    @Test
    void should_throw_UserNotFoundException_when_id_not_found() {
        final var id = UserId.generate();
        when(repository.findById(id)).thenReturn(empty());

        assertThatThrownBy(() -> service.execute(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void should_embed_errorCode_USER_NOT_FOUND_in_exception() {
        final var id = UserId.generate();
        when(repository.findById(id)).thenReturn(empty());
        assertThatThrownBy(() -> service.execute(id))
                .isInstanceOf(UserNotFoundException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND.name());
    }

    @Test
    void should_include_id_value_in_exception_message() {
        final var id = UserId.generate();
        when(repository.findById(id)).thenReturn(empty());
        assertThatThrownBy(() -> service.execute(id))
                .hasMessageContaining(id.value().toString());
    }

    @Test
    void should_throw_NullPointerException_when_id_is_null() {
        assertThatNullPointerException()
                .isThrownBy(() -> service.execute(null))
                .withMessageContaining("id must not be null");
    }

    @Test
    void should_never_call_repository_when_id_is_null() {
        catchThrowable(() -> service.execute(null));
        verifyNoInteractions(repository);
    }
}