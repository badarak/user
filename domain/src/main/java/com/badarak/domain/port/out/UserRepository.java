package com.badarak.domain.port.out;

import com.badarak.domain.model.Email;
import com.badarak.domain.model.User;
import com.badarak.domain.model.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(UserId id);

    void save(User user);

    void deleteById(UserId id);

    boolean existsByEmail(Email email);

    Page<User> findAll(Pageable pageable);
}
