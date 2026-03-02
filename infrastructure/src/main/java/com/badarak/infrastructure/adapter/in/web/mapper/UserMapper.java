package com.badarak.infrastructure.adapter.in.web.mapper;


import com.badarak.domain.model.User;
import com.badarak.domain.model.UserId;
import com.badarak.domain.port.in.CreateUserUseCase.CreateUserCommand;
import com.badarak.domain.port.in.ListUsersUseCase.UserPage;
import com.badarak.domain.port.in.UpdateUserUseCase.UpdateUserCommand;
import com.badarak.infrastructure.adapter.in.web.dto.*;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
public class UserMapper {

    public CreateUserCommand toCommand(CreateUserRequest request) {
        requireNonNull(request, "request must not be null");
        return new CreateUserCommand(
                request.email(),
                request.firstName(),
                request.lastName()
        );
    }


    public CreateUserResponse toResponse(UserId userId) {
        requireNonNull(userId, "userId must not be null");
        return new CreateUserResponse(userId.value());
    }

    public UserResponse toUserResponse(User user) {
        requireNonNull(user, "user must not be null");
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

    public UserPageResponse toPageResponse(UserPage page) {
        requireNonNull(page, "page must not be null");
        return new UserPageResponse(
                page.content().stream().map(this::toUserResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    public UpdateUserCommand toUpdateUserCommand(UserId id, UpdateUserRequest req) {
        requireNonNull(req, "req must not be null");
        requireNonNull(id, "id must not be null");
        return new UpdateUserCommand(id, req.firstName(), req.lastName());
    }
}