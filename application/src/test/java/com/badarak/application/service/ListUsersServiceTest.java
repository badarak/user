package com.badarak.application.service;

import com.badarak.domain.model.*;
import com.badarak.domain.port.out.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class ListUsersServiceTest {

    private UserRepository repository;
    private ListUsersService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        service = new ListUsersService(repository);
    }


    private static User user(String email, UserStatus status) {
        return User.reconstitute(
                UserId.generate(),
                new Email(email),
                new UserName("A", "B"),
                status,
                Instant.now(),
                Instant.now()
        );
    }

    private static UserPage emptyPage(int page, int size) {
        return new UserPage(List.of(), page, size, 0L, 0);
    }

    private static UserPage pageOf(User... users) {
        return new UserPage(List.of(users), 0, 20, users.length, 1);
    }

    @Test
    void should_delegate_query_unchanged_to_repository() {
        final var query = new UserQuery(2, 10, UserStatus.ACTIVE);
        when(repository.findAll(query)).thenReturn(emptyPage(2, 10));
        service.execute(query);
        verify(repository).findAll(query);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void should_never_write_to_repository_during_list() {
        when(repository.findAll(any())).thenReturn(emptyPage(0, 20));
        service.execute(UserQuery.defaultQuery());
        verify(repository, never()).save(any());
    }

    @Test
    void should_return_page_with_correct_content() {
        final var u1 = user("a@example.com", UserStatus.ACTIVE);
        final var u2 = user("b@example.com", UserStatus.ACTIVE);
        when(repository.findAll(any())).thenReturn(pageOf(u1, u2));

        final var result = service.execute(UserQuery.defaultQuery());

        assertThat(result.content()).containsExactly(u1, u2);
    }

    @Test
    void should_return_correct_pagination_metadata() {
        final var page = new UserPage(List.of(), 3, 5, 42L, 9);
        when(repository.findAll(any())).thenReturn(page);

        final var result = service.execute(new UserQuery(3, 5, null));

        assertThat(result.page()).isEqualTo(3);
        assertThat(result.size()).isEqualTo(5);
        assertThat(result.totalElements()).isEqualTo(42L);
        assertThat(result.totalPages()).isEqualTo(9);
    }

    @Test
    void should_return_empty_content_when_no_users_match() {
        when(repository.findAll(any())).thenReturn(emptyPage(0, 20));

        final var result = service.execute(UserQuery.defaultQuery());

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void should_pass_ACTIVE_status_filter_to_repository() {
        when(repository.findAll(argThat(q -> q.status() == UserStatus.ACTIVE)))
                .thenReturn(emptyPage(0, 20));

        service.execute(new UserQuery(0, 20, UserStatus.ACTIVE));

        verify(repository).findAll(argThat(q -> q.status() == UserStatus.ACTIVE));
    }

    @Test
    void should_pass_INACTIVE_status_filter_to_repository() {
        when(repository.findAll(argThat(q -> q.status() == UserStatus.INACTIVE)))
                .thenReturn(emptyPage(0, 20));

        service.execute(new UserQuery(0, 20, UserStatus.INACTIVE));

        verify(repository).findAll(argThat(q -> q.status() == UserStatus.INACTIVE));
    }

    @Test
    void should_pass_null_status_when_no_filter_requested() {
        when(repository.findAll(argThat(q -> q.status() == null)))
                .thenReturn(emptyPage(0, 20));

        service.execute(new UserQuery(0, 20, null));

        verify(repository).findAll(argThat(q -> q.status() == null));
    }

    @Test
    void should_return_only_active_users_when_filter_is_ACTIVE() {
        final var active = user("a@example.com", UserStatus.ACTIVE);
        when(repository.findAll(argThat(q -> q.status() == UserStatus.ACTIVE)))
                .thenReturn(pageOf(active));

        final var result = service.execute(new UserQuery(0, 20, UserStatus.ACTIVE));

        assertThat(result.content()).isNotEmpty();
        assertThat(result.content()).allMatch(u -> u.getStatus() == UserStatus.ACTIVE);
    }

    @Test
    void should_pass_page_number_correctly_to_repository() {
        when(repository.findAll(argThat(q -> q.page() == 5))).thenReturn(emptyPage(5, 10));

        service.execute(new UserQuery(5, 10, null));

        verify(repository).findAll(argThat(q -> q.page() == 5));
    }

    @ParameterizedTest(name = "size={0}")
    @ValueSource(ints = {1, 10, 50, 100})
    void should_pass_page_size_correctly_to_repository(int size) {
        when(repository.findAll(argThat(q -> q.size() == size))).thenReturn(emptyPage(0, size));

        service.execute(new UserQuery(0, size, null));

        verify(repository).findAll(argThat(q -> q.size() == size));
    }

    @Test
    void should_throw_IllegalArgumentException_when_page_is_negative() {
        assertThatThrownBy(() -> new UserQuery(-1, 20, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page must be >= 0");
    }

    @Test
    void should_throw_IllegalArgumentException_when_size_is_zero() {
        assertThatThrownBy(() -> new UserQuery(0, 0, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size must be >= 1");
    }

    @Test
    void should_throw_IllegalArgumentException_when_size_exceeds_100() {
        assertThatThrownBy(() -> new UserQuery(0, 101, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size must be <= 100");
    }

    @Test
    void should_throw_NullPointerException_when_query_is_null() {
        assertThatNullPointerException()
                .isThrownBy(() -> service.execute(null))
                .withMessageContaining("query must not be null");
    }

    @Test
    void should_never_call_repository_when_query_is_null() {
        catchThrowable(() -> service.execute(null));
        verifyNoInteractions(repository);
    }
}