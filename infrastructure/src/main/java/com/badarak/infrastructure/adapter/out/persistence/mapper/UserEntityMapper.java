package com.badarak.infrastructure.adapter.out.persistence.mapper;

import com.badarak.domain.model.*;
import com.badarak.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
public class UserEntityMapper {

    public UserEntity toEntity(User user) {
        Objects.requireNonNull(user, "user must not be null");
        return new UserEntity(
                user.getId().value(),
                user.getEmail().value(),
                user.getName().firstName(),
                user.getName().lastName(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public User toDomain(UserEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return User.reconstitute(
                new UserId(entity.getId()),
                new Email(entity.getEmail()),
                new UserName(entity.getFirstName(), entity.getLastName()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}