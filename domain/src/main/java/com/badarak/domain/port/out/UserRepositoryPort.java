package com.badarak.domain.port.out;

import com.badarak.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findById(UUID id);

    User save(User user);

    void deleteById(UUID id);
}
