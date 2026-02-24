package com.badarak.infrastructure.adapter.in.web.controller;

import com.badarak.domain.model.UserId;
import com.badarak.domain.model.UserStatus;
import com.badarak.domain.port.in.CreateUserUseCase;
import com.badarak.domain.port.in.GetUserUseCase;
import com.badarak.domain.port.in.ListUsersUseCase;
import com.badarak.domain.port.in.ListUsersUseCase.UserQuery;
import com.badarak.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.badarak.infrastructure.adapter.in.web.dto.CreateUserResponse;
import com.badarak.infrastructure.adapter.in.web.dto.UserPageResponse;
import com.badarak.infrastructure.adapter.in.web.dto.UserResponse;
import com.badarak.infrastructure.adapter.in.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@RestController
@Validated
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management API")
public class UserController implements UserControllerDocumentation {
    private final CreateUserUseCase createUser;
    private final GetUserUseCase getUser;
    private final ListUsersUseCase listUsers;
    private final UserMapper mapper;

    UserController(CreateUserUseCase createUser, GetUserUseCase getUser, ListUsersUseCase listUsers, UserMapper mapper) {
        this.createUser = requireNonNull(createUser);
        this.getUser = requireNonNull(getUser);
        this.listUsers = requireNonNull(listUsers);
        this.mapper = requireNonNull(mapper);
    }

    @PostMapping
    @Override
    public ResponseEntity<CreateUserResponse> create(@RequestBody CreateUserRequest req) {

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
        return ResponseEntity.ok(mapper.toUserResponse(getUser.execute(new UserId(id))));
    }

//
//    @PutMapping("/{id}")
//    @Override
//    public ResponseEntity<UserResponse> update(@PathVariable("id") UUID id, @RequestBody CreateOrUpdateUserRequest req) {
//        return ok(of(userService.updateUser(id, req.name(), req.email())));
//    }
//
//    @DeleteMapping("/{id}")
//    @Override
//    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
//        userService.deleteUser(id);
//        return ResponseEntity.noContent().build();
//    }
//

    @GetMapping
    @Override
    public ResponseEntity<UserPageResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UserStatus status) {

        return ResponseEntity.ok(mapper.toPageResponse(listUsers.execute(new UserQuery(page, size, status))));
    }
}
