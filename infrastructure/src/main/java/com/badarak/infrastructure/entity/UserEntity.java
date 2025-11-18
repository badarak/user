package com.badarak.infrastructure.entity;

import com.badarak.domain.model.User;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    public UserEntity() {
    }

    public User toDomain() {
        return new User(id, name, email);
    }

    public static UserEntity of(User user) {
        return new UserEntity(user.getId(), user.getName(), user.getEmail());
    }

    public UserEntity(UUID id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
