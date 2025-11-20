package com.badarak.domain.port.in;

import com.badarak.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserServicePort {
    User findById(UUID id);
    User createUser(String name, String email);

    void deleteUser(UUID uuid);

    User updateUser(UUID id, String name, String email);

    Page<User> listUsers(Pageable pageable);
}
