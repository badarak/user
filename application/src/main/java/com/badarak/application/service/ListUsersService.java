package com.badarak.application.service;

import com.badarak.domain.model.UserPage;
import com.badarak.domain.model.UserQuery;
import com.badarak.domain.port.in.ListUsersUseCase;
import com.badarak.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

@Service
@Transactional(readOnly = true)
public class ListUsersService implements ListUsersUseCase {
    private final UserRepository repository;

    ListUsersService(UserRepository repository) {
        this.repository = requireNonNull(repository);
    }

    @Override
    public UserPage execute(UserQuery query) {
        requireNonNull(query, "query must not be null");
        return repository.findAll(query);
    }
}
