package com.badarak.infrastructure.adapter.out.persistence;

import com.badarak.domain.model.User;
import com.badarak.domain.model.UserId;
import com.badarak.domain.model.UserQuery;
import com.badarak.domain.model.UserStatus;
import com.badarak.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.badarak.infrastructure.adapter.out.persistence.mapper.UserEntityMapper;
import com.badarak.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;


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

    @Nested
    @DisplayName("findAll() — without filter")
    class FindAll {

        @Test
        void should_return_empty_page_when_no_users_in_database() {
            final var result = adapter.findAll(UserQuery.defaultQuery());
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }

        @Test
        void should_return_all_users_on_first_page() {
            save("a@example.com", UserStatus.ACTIVE);
            save("b@example.com", UserStatus.ACTIVE);
            save("c@example.com", UserStatus.INACTIVE);

            final var result = adapter.findAll(UserQuery.defaultQuery());

            assertThat(result.totalElements()).isEqualTo(3);
            assertThat(result.content()).hasSize(3);
        }

        @Test
        void should_respect_page_size_limit() {
            save("a@example.com", UserStatus.ACTIVE);
            save("b@example.com", UserStatus.ACTIVE);
            save("c@example.com", UserStatus.ACTIVE);

            final var result = adapter.findAll(new UserQuery(0, 2, null));

            assertThat(result.content()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(3);
        }

        @Test
        void should_return_second_page_correctly() {
            save("a@example.com", UserStatus.ACTIVE);
            save("b@example.com", UserStatus.ACTIVE);
            save("c@example.com", UserStatus.ACTIVE);

            final var result = adapter.findAll(new UserQuery(1, 2, null));

            assertThat(result.content()).hasSize(1);
            assertThat(result.page()).isEqualTo(1);
        }

        @Test
        void should_compute_totalPages_correctly() {
            for (int i = 0; i < 5; i++)
                save("user" + i + "@x.com", UserStatus.ACTIVE);

            final var result = adapter.findAll(new UserQuery(0, 2, null));

            assertThat(result.totalPages()).isEqualTo(3);
        }

        @ParameterizedTest(name = "page size = {0}")
        @ValueSource(ints = {1, 10, 20, 50, 100})
        void should_accept_all_valid_page_sizes(int size) {
            save("x@example.com", UserStatus.ACTIVE);
            assertThatNoException().isThrownBy(
                    () -> adapter.findAll(new UserQuery(0, size, null)));
        }

        @Test
        void should_include_both_ACTIVE_and_INACTIVE_when_no_filter() {
            save("act@example.com", UserStatus.ACTIVE);
            save("inact@example.com", UserStatus.INACTIVE);

            final var result = adapter.findAll(UserQuery.defaultQuery());

            assertThat(result.content())
                    .extracting(User::getStatus)
                    .contains(UserStatus.ACTIVE, UserStatus.INACTIVE);
        }

        @Test
        void should_order_results_by_createdAt_descending() {
            save("old@example.com", UserStatus.ACTIVE);
            save("recent@example.com", UserStatus.ACTIVE);

            final var result = adapter.findAll(UserQuery.defaultQuery());

            final var dates = result.content().stream()
                    .map(User::getCreatedAt).toList();
            assertThat(dates).isSortedAccordingTo((a, b) -> b.compareTo(a));
        }

        @Test
        void should_populate_page_metadata_correctly() {
            save("a@example.com", UserStatus.ACTIVE);
            save("b@example.com", UserStatus.ACTIVE);

            final var result = adapter.findAll(new UserQuery(0, 10, null));

            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findAll() — filtered by status")
    class FindAllByStatus {

        @Test
        void should_return_only_ACTIVE_users_when_status_filter_is_ACTIVE() {
            save("active@example.com", UserStatus.ACTIVE);
            save("inactive@example.com", UserStatus.INACTIVE);

            final var result = adapter.findAll(new UserQuery(0, 20, UserStatus.ACTIVE));

            assertThat(result.content())
                    .isNotEmpty()
                    .allMatch(u -> u.getStatus() == UserStatus.ACTIVE);
        }

        @Test
        void should_return_only_INACTIVE_users_when_status_filter_is_INACTIVE() {
            save("active@example.com", UserStatus.ACTIVE);
            save("inactive@example.com", UserStatus.INACTIVE);

            final var result = adapter.findAll(new UserQuery(0, 20, UserStatus.INACTIVE));

            assertThat(result.content())
                    .isNotEmpty()
                    .allMatch(u -> u.getStatus() == UserStatus.INACTIVE);
        }

        @Test
        void should_return_empty_page_when_no_INACTIVE_users_exist() {
            save("active@example.com", UserStatus.ACTIVE);

            final var result = adapter.findAll(new UserQuery(0, 20, UserStatus.INACTIVE));

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }

        @Test
        void should_count_only_matching_status_in_totalElements() {
            save("a1@x.com", UserStatus.ACTIVE);
            save("a2@x.com", UserStatus.ACTIVE);
            save("i1@x.com", UserStatus.INACTIVE);

            final var result = adapter.findAll(new UserQuery(0, 20, UserStatus.ACTIVE));

            assertThat(result.totalElements()).isEqualTo(2);
        }

        @Test
        void should_paginate_filtered_results_correctly() {
            for (int i = 0; i < 5; i++)
                save("a" + i + "@x.com", UserStatus.ACTIVE);

            save("i@x.com", UserStatus.INACTIVE);

            final var result = adapter.findAll(new UserQuery(0, 3, UserStatus.ACTIVE));

            assertThat(result.content()).hasSize(3);
            assertThat(result.totalElements()).isEqualTo(5);
            assertThat(result.totalPages()).isEqualTo(2);
        }
    }
}