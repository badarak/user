package com.badarak.domain.port.out;

import com.badarak.domain.model.*;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId id);

    void save(User user);

    void deleteById(UserId id);

    boolean existsByEmail(Email email);

    UserPage findAll(UserQuery query);
}
