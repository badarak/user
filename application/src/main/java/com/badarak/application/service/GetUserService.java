package com.badarak.application.service;

import com.badarak.domain.exception.UserNotFoundException;
import com.badarak.domain.model.User;
import com.badarak.domain.model.UserId;
import com.badarak.domain.port.in.GetUserUseCase;
import com.badarak.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class GetUserService implements GetUserUseCase {

    private final UserRepository repository;

    GetUserService(UserRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public User execute(UserId id) {
        Objects.requireNonNull(id, "id must not be null");
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
