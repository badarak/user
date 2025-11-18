package com.badarak.domain.port.in;

import com.badarak.domain.model.User;

import java.util.UUID;

public interface UserServicePort {
    User findById(UUID id);
    User createUser(User user);

    void deleteUser(UUID uuid);
}
