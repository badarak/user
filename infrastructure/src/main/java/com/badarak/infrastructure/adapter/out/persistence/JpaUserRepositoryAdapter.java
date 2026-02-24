package com.badarak.infrastructure.adapter.out.persistence;

import com.badarak.domain.model.Email;
import com.badarak.domain.model.User;
import com.badarak.domain.model.UserId;
import com.badarak.domain.port.in.ListUsersUseCase.UserPage;
import com.badarak.domain.port.in.ListUsersUseCase.UserQuery;
import com.badarak.domain.port.out.UserRepository;
import com.badarak.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.badarak.infrastructure.adapter.out.persistence.mapper.UserEntityMapper;
import com.badarak.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {
    private final SpringDataUserRepository jpaRepository;
    private final UserEntityMapper mapper;

    public JpaUserRepositoryAdapter(SpringDataUserRepository repo, UserEntityMapper mapper) {
        this.jpaRepository = requireNonNull(repo);
        this.mapper = requireNonNull(mapper);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(Objects.requireNonNull(id).value())
                .map(mapper::toDomain);
    }

    @Override
    public void save(User user) {
        jpaRepository.save(mapper.toEntity(requireNonNull(user)));
    }

    @Override
    public void deleteById(UserId id) {
        jpaRepository.deleteById(requireNonNull(id).value());
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(requireNonNull(email).value());
    }

    @Override
    public UserPage findAll(UserQuery query) {
        Objects.requireNonNull(query);

        final var pageable = PageRequest.of(query.page(), query.size(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        final Page<UserEntity> page =
                query.status() != null
                        ? jpaRepository.findAllByStatus(query.status(), pageable)
                        : jpaRepository.findAll(pageable);

        return new UserPage(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
