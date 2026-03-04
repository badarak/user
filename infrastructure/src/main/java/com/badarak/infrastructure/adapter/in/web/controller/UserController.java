package com.badarak.infrastructure.adapter.in.web.controller;

import com.badarak.domain.model.UserId;
import com.badarak.domain.model.UserQuery;
import com.badarak.domain.model.UserStatus;
import com.badarak.domain.port.in.*;
import com.badarak.infrastructure.adapter.in.web.dto.*;
import com.badarak.infrastructure.adapter.in.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management API")
public class UserController implements UserControllerDocumentation {
    private final CreateUserUseCase createUser;
    private final GetUserUseCase getUser;
    private final ListUsersUseCase listUsers;
    private final DeleteUserUseCase deleteUser;
    private final UpdateUserUseCase updateUser;
    private final UserMapper mapper;

    UserController(
            CreateUserUseCase createUser,
            GetUserUseCase getUser,
            ListUsersUseCase listUsers,
            DeleteUserUseCase deleteUser,
            UpdateUserUseCase updateUser,
            UserMapper mapper
    ) {
        this.createUser = requireNonNull(createUser);
        this.getUser = requireNonNull(getUser);
        this.listUsers = requireNonNull(listUsers);
        this.deleteUser = requireNonNull(deleteUser);
        this.updateUser = requireNonNull(updateUser);
        this.mapper = requireNonNull(mapper);
    }

    @PostMapping
    @Override
    public ResponseEntity<CreateUserResponse> create(@Valid @RequestBody CreateUserRequest req) {

        final var command = mapper.toCommand(req);
        final var userId = createUser.execute(command);
        final var response = mapper.toResponse(userId);

        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userId.value())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }


    @GetMapping("/{id}")
    @Override
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ok(mapper.toUserResponse(getUser.execute(new UserId(id))));
    }


    @PutMapping("/{id}")
    @Override
    public ResponseEntity<UserResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody UpdateUserRequest req) {
        UserId userId = new UserId(id);
        return ResponseEntity.ok(
                mapper.toUserResponse(
                        updateUser.execute(mapper.toUpdateUserCommand(userId, req))
                )
        );
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        deleteUser.execute(new UserId(id));
        return ResponseEntity.noContent().build();
    }


    @GetMapping
    @Override
    public ResponseEntity<UserPageResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UserStatus status) {

        return ok(mapper.toPageResponse(listUsers.execute(new UserQuery(page, size, status))));
    }
}
