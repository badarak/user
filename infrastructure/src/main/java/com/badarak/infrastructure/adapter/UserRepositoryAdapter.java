package com.badarak.infrastructure.adapter;

import com.badarak.domain.model.User;
import com.badarak.domain.port.out.UserRepositoryPort;
import com.badarak.infrastructure.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.badarak.infrastructure.entity.UserEntity.of;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {
    private SpringDataUserRepository repo;

    public UserRepositoryAdapter(SpringDataUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repo.findById(id).map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        return repo.save(of(user)).toDomain();
    }

    @Override
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return repo.findAll(pageable).map(UserEntity::toDomain);
    }
}
