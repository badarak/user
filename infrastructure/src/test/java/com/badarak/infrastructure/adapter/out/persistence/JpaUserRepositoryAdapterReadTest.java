package com.badarak.infrastructure.adapter.out.persistence;

import com.badarak.domain.model.UserId;
import com.badarak.domain.model.UserStatus;
import com.badarak.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.badarak.infrastructure.adapter.out.persistence.mapper.UserEntityMapper;
import com.badarak.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaUserRepositoryAdapter.class, UserEntityMapper.class})
@DisplayName("JpaUserRepositoryAdapter — READ — tests persistence")
class JpaUserRepositoryAdapterReadTest {

    @Autowired
    JpaUserRepositoryAdapter adapter;
    @Autowired
    SpringDataUserRepository jpa;

    private UserEntity save(String email, UserStatus status) {
        final var now = Instant.now();
        final var e = new UserEntity(
                UUID.randomUUID(),
                email,
                "First",
                "Last",
                status,
                now,
                now
        );
        return jpa.saveAndFlush(e);
    }

    @Nested
    class FindById {

        @Test
        void should_return_user_when_id_exists_in_database() {
            final var entity = save("alice@example.com", UserStatus.ACTIVE);
            final var id = new UserId(entity.getId());

            final var result = adapter.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getId().value()).isEqualTo(entity.getId());
        }

        @Test
        void should_map_email_correctly_from_database_row() {
            final var entity = save("alice@example.com", UserStatus.ACTIVE);
            final var result = adapter.findById(new UserId(entity.getId()));

            assertThat(result).isPresent();
            assertThat(result.get().getEmail().value()).isEqualTo("alice@example.com");
        }

        @Test
        void should_map_firstName_and_lastName_from_database() {
            final var entity = save("bob@example.com", UserStatus.ACTIVE);
            final var result = adapter.findById(new UserId(entity.getId())).orElseThrow();

            assertThat(result.getName().firstName()).isEqualTo("First");
            assertThat(result.getName().lastName()).isEqualTo("Last");
        }

        @Test
        void should_map_status_ACTIVE_from_database() {
            final var entity = save("active@example.com", UserStatus.ACTIVE);
            final var result = adapter.findById(new UserId(entity.getId())).orElseThrow();

            assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        void should_map_status_INACTIVE_from_database() {
            final var entity = save("inactive@example.com", UserStatus.INACTIVE);
            final var result = adapter.findById(new UserId(entity.getId())).orElseThrow();

            assertThat(result.getStatus()).isEqualTo(UserStatus.INACTIVE);
        }

        @Test
        void should_map_createdAt_and_updatedAt_from_database() {
            final var entity = save("ts@example.com", UserStatus.ACTIVE);
            final var result = adapter.findById(new UserId(entity.getId())).orElseThrow();

            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
        }

        @Test
        void should_return_empty_Optional_when_id_does_not_exist() {
            final var result = adapter.findById(UserId.generate());
            assertThat(result).isEmpty();
        }

        @Test
        void should_return_empty_Optional_for_random_UUID() {
            save("x@example.com", UserStatus.ACTIVE);
            assertThat(adapter.findById(UserId.generate())).isEmpty();
        }
    }
}