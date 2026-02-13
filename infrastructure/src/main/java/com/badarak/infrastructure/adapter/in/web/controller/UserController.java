package com.badarak.infrastructure.adapter.in.web.controller;

import com.badarak.domain.port.in.CreateUserUseCase;
import com.badarak.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.badarak.infrastructure.adapter.in.web.dto.CreateUserResponse;
import com.badarak.infrastructure.adapter.in.web.mapper.UserRequestMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management API")
public class UserController implements UserControllerDocumentation {
    private final CreateUserUseCase createUserUseCase;
    private final UserRequestMapper mapper;

    UserController(CreateUserUseCase createUserUseCase, UserRequestMapper mapper) {
        this.createUserUseCase = requireNonNull(createUserUseCase);
        this.mapper = requireNonNull(mapper);
    }

    @PostMapping
    @Override
    public ResponseEntity<CreateUserResponse> create(@Valid @RequestBody CreateUserRequest req) {

        final var command = mapper.toCommand(req);
        final var userId = createUserUseCase.execute(command);
        final var response = mapper.toResponse(userId);

        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userId.value())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

//    @GetMapping("/{id}")
//    @Override
//    public ResponseEntity<UserResponse> get(@PathVariable("id") UUID id) {
//        return ok(of(userService.findById(id)));
//    }
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
//    @GetMapping("")
//    @Override
//    public ResponseEntity<Page<UserResponse>> list(Pageable pageable) {
//        Page<UserResponse> page = userService.listUsers(pageable).map(UserResponse::of);
//        return ok(page);
//    }
}
