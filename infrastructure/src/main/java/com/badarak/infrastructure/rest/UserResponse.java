package com.badarak.infrastructure.rest;

import com.badarak.domain.model.User;

import java.util.UUID;

public record UserResponse(UUID id, String name, String email) {

    static UserResponse of(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
