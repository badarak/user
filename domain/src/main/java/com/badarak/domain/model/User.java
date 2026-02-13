package com.badarak.domain.model;

import com.badarak.domain.event.DomainEvent;
import com.badarak.domain.event.UserCreatedEvent;
import com.badarak.domain.exception.UserAlreadyInactiveException;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;

@Getter
public class User {

    private final UserId id;
    private final Email email;
    private final UserName name;
    private UserStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private User(UserId id, Email email, UserName name, UserStatus status, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = createdAt;
    }

    public static User create(Email email, UserName name) {
        final var user = new User(
                UserId.generate(),
                email,
                name,
                UserStatus.ACTIVE,
                Instant.now()
        );
        user.registerEvent(new UserCreatedEvent(user.id, user.email, user.name));
        return user;
    }

    public static User reconstitute(
            UserId id,
            Email email,
            UserName name,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt) {

        final var user = new User(id, email, name, status, createdAt);
        user.updatedAt = updatedAt;
        return user;
    }

    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            throw new UserAlreadyInactiveException(this.id);
        }
        this.status = UserStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public List<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    private void registerEvent(DomainEvent event) {
        domainEvents.add(Objects.requireNonNull(event));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return Objects.equals(id, u.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return format("User{id=%s, email=%s, status=%s", id.value(), email.value(), status.name());
    }
}