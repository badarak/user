package com.badarak.application.service;

import com.badarak.domain.exception.UserNotFoundException;
import com.badarak.domain.model.User;
import com.badarak.domain.model.UserName;
import com.badarak.domain.port.in.UpdateUserUseCase;
import com.badarak.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

@Service
@Transactional
public class UpdateUserService implements UpdateUserUseCase {
    private final UserRepository repository;

    UpdateUserService(UserRepository repository) {
        this.repository = requireNonNull(repository);
    }

    @Override
    public User execute(UpdateUserCommand command) {
        requireNonNull(command, "command must not be null");

        final var user = repository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        user.updateName(new UserName(command.firstName(), command.lastName()));
        repository.save(user);
        return user;
    }
}
