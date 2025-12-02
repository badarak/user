package com.badarak.domain.event;

import com.badarak.domain.model.User;

import java.util.UUID;

public record UserCreated(UUID id, String email) {
    public static UserCreated of(User saved) {
        return new UserCreated(saved.getId(), saved.getEmail());
    }
}
