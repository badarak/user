package com.badarak.application.service;

import com.badarak.domain.exception.UserNotFoundException;
import com.badarak.domain.model.UserId;
import com.badarak.domain.port.in.DeleteUserUseCase;
import com.badarak.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

@Service
@Transactional
public class DeleteUserService implements DeleteUserUseCase {
    private final UserRepository repository;

    public DeleteUserService(UserRepository repository) {
        requireNonNull(repository);
        this.repository = repository;
    }

    @Override
    public void execute(UserId id) {
        requireNonNull(id, "id must not be null");

        final var user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.deactivate();
        repository.save(user);
    }
}
