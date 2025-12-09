package com.badarak.infrastructure.entity;

import com.badarak.domain.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    public User toDomain() {
        return new User(id, name, email);
    }

    public static UserEntity of(User user) {
        return new UserEntity(user.getId(), user.getName(), user.getEmail());
    }
}
