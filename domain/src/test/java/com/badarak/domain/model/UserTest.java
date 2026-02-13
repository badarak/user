package com.badarak.domain.model;

import com.badarak.domain.event.UserCreatedEvent;
import com.badarak.domain.exception.UserAlreadyInactiveException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {
    @Test
    void should_create_active_user_with_generated_id() {
        final var user = createTestUser();

        assertThat(user.getId()).isNotNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void should_register_UserCreatedEvent_after_create() {
        final var user = createTestUser();

        assertThat(user.domainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(UserCreatedEvent.class);

        final var event = (UserCreatedEvent) user.domainEvents().getFirst();
        assertThat(event.userId()).isEqualTo(user.getId());
        assertThat(event.email()).isEqualTo(user.getEmail());
    }

    @Test
    void should_domain_events_as_unmodifiable_list() {
        final var user = createTestUser();
        assertThatThrownBy(() -> user.domainEvents().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_set_status_to_inactive() {
        final var user = createTestUser();
        user.deactivate();
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void should_throws_UserAlreadyInactiveException_when_deactivate_already_inactive() {
        final var user = createTestUser();
        user.deactivate();

        assertThatThrownBy(user::deactivate)
                .isInstanceOf(UserAlreadyInactiveException.class);
    }

    @Test
    void should_empty_list_after_clear() {
        final var user = createTestUser();
        assertThat(user.domainEvents()).isNotEmpty();

        user.clearDomainEvents();

        assertThat(user.domainEvents()).isEmpty();
    }

    private static User createTestUser() {
        return User.create(
                new Email("test@example.com"),
                new UserName("John", "Doe")
        );
    }
}