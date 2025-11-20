package com.badarak.domain.service;

import com.badarak.domain.exception.ResourceNotFoundException;
import com.badarak.domain.model.User;
import com.badarak.domain.port.in.UserServicePort;
import com.badarak.domain.port.out.UserRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService implements UserServicePort {
    private final UserRepositoryPort repository;

    public UserService(UserRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public User findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID : " + id));
    }

    @Override
    public User createUser(String name, String email) {
        return repository.save(new User(UUID.randomUUID(), name, email));
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
