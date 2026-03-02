package com.badarak.domain.event;

import com.badarak.domain.model.Email;
import com.badarak.domain.model.UserId;
import com.badarak.domain.model.UserName;

import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(
        UUID eventId,
        Instant occurredOn,
        UserId userId,
        Email email,
        UserName name
) implements DomainEvent {

    public UserCreatedEvent(UserId userId, Email email, UserName name) {
        this(UUID.randomUUID(), Instant.now(), userId, email, name);
    }
}