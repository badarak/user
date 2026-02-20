package com.badarak.infrastructure.adapter.in.web.mapper;

import com.badarak.domain.model.*;
import com.badarak.infrastructure.adapter.in.web.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("UserMapper — tests READ (RtoUserResponse)")
public class UserMapperReadTest {

    private final UserMapper mapper = new UserMapper();

    private static User activeUser() {
        final var id = UserId.generate();
        final var now = Instant.now();
        return User.reconstitute(id,
                new Email("john@example.com"),
                new UserName("John", "Doe"),
                UserStatus.ACTIVE,
                now.minusSeconds(60),
                now
        );
    }

    private static User inactiveUser() {
        final var id = UserId.generate();
        final var now = Instant.now();
        return User.reconstitute(id,
                new Email("jane@example.com"),
                new UserName("Jane", "Smith"),
                UserStatus.INACTIVE,
                now.minusSeconds(3600),
                now
        );
    }

    @Nested
    class ToUserResponse {

        @Test
        void should_map_id_to_UUID_value() {
            final var user = activeUser();
            final var result = mapper.toUserResponse(user);
            assertThat(result.id()).isEqualTo(user.getId().value());
        }

        @Test
        void should_map_email_string_value() {
            final var user = activeUser();
            final var result = mapper.toUserResponse(user);
            assertThat(result.email()).isEqualTo("john@example.com");
        }

        @Test
        void should_map_firstName_from_UserName() {
            final var result = mapper.toUserResponse(activeUser());
            assertThat(result.firstName()).isEqualTo("John");
        }

        @Test
        void should_map_lastName_from_UserName() {
            final var result = mapper.toUserResponse(activeUser());
            assertThat(result.lastName()).isEqualTo("Doe");
        }

        @Test
        void should_map_status_ACTIVE() {
            final var result = mapper.toUserResponse(activeUser());
            assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        void should_map_status_INACTIVE() {
            final var result = mapper.toUserResponse(inactiveUser());
            assertThat(result.status()).isEqualTo(UserStatus.INACTIVE);
        }

        @Test
        void should_map_createdAt_timestamp() {
            final var user = activeUser();
            final var result = mapper.toUserResponse(user);
            assertThat(result.createdAt()).isEqualTo(user.getCreatedAt());
        }

        @Test
        void should_map_updatedAt_timestamp() {
            final var user = activeUser();
            final var result = mapper.toUserResponse(user);
            assertThat(result.updatedAt()).isEqualTo(user.getUpdatedAt());
        }

        @Test
        void should_map_all_seven_fields_in_a_single_call() {
            final var user = activeUser();
            final var result = mapper.toUserResponse(user);
            assertThat(result)
                    .isNotNull()
                    .extracting(
                            UserResponse::id,
                            UserResponse::email,
                            UserResponse::firstName,
                            UserResponse::lastName,
                            UserResponse::status,
                            UserResponse::createdAt,
                            UserResponse::updatedAt)
                    .doesNotContainNull();
        }

        @Test
        void should_produce_different_responses_for_different_users() {
            final var result1 = mapper.toUserResponse(activeUser());
            final var result2 = mapper.toUserResponse(inactiveUser());
            assertThat(result1.id()).isNotEqualTo(result2.id());
            assertThat(result1.email()).isNotEqualTo(result2.email());
        }

        @Test
        void should_throw_NullPointerException_when_user_is_null() {
            assertThatNullPointerException()
                    .isThrownBy(() -> mapper.toUserResponse(null));
        }
    }
}
