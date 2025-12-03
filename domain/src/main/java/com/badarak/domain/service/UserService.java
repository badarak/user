package com.badarak.domain.service;

import com.badarak.domain.event.UserCreated;
import com.badarak.domain.exception.ResourceNotFoundException;
import com.badarak.domain.model.User;
import com.badarak.domain.port.in.UserEventPublisher;
import com.badarak.domain.port.in.UserServicePort;
import com.badarak.domain.port.out.UserRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public class UserService implements UserServicePort {
    private final UserRepositoryPort repository;
    private final UserEventPublisher publish;

    public UserService(UserRepositoryPort repository, UserEventPublisher eventPublisher) {
        this.repository = repository;
        this.publish = eventPublisher;
    }

    @Override
    public User findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID : " + id));
    }

    @Override
    public User createUser(String name, String email) {
        User saved = repository.save(new User(UUID.randomUUID(), name, email));
        publish.publish(UserCreated.of(saved));
        return saved;
    }

    @Override
    public void deleteUser(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public User updateUser(UUID id, String name, String email) {
        User existing = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID : " + id));

        return repository.save(new User(existing.getId(), name, email));
    }

    @Override
    public Page<User> listUsers(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
