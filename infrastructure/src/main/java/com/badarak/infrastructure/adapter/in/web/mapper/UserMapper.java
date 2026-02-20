package com.badarak.infrastructure.adapter.in.web.mapper;


import com.badarak.domain.model.User;
import com.badarak.domain.model.UserId;
import com.badarak.domain.port.in.CreateUserUseCase.CreateUserCommand;
import com.badarak.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.badarak.infrastructure.adapter.in.web.dto.CreateUserResponse;
import com.badarak.infrastructure.adapter.in.web.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
public class UserMapper {

    public CreateUserCommand toCommand(CreateUserRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new CreateUserCommand(
                request.email(),
                request.firstName(),
                request.lastName()
        );
    }


    public CreateUserResponse toResponse(UserId userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return new CreateUserResponse(userId.value());
    }

    public UserResponse toUserResponse(User user) {
        Objects.requireNonNull(user, "user must not be null");
        return new UserResponse(
                user.getId().value(),
                user.getEmail().value(),
                user.getName().firstName(),
                user.getName().lastName(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}