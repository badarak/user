package com.badarak.infrastructure.rest;

import com.badarak.domain.port.in.UserServicePort;
import com.badarak.domain.port.out.UserRepositoryPort;
import com.badarak.domain.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.badarak.infrastructure.rest.UserResponse.of;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/users")
public class UserController implements UserControllerDocumentation {
    private final UserServicePort userService;

    public UserController(UserRepositoryPort repository) {
        this.userService = new UserService(repository);
    }

    @PostMapping("")
    @Override
    public ResponseEntity<UserResponse> create(@RequestBody CreateOrUpdateUserRequest req) {
        return status(HttpStatus.CREATED)
                .body(of(userService.createUser(req.name(), req.email())));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<UserResponse> get(@PathVariable("id") UUID id) {
        return ok(of(userService.findById(id)));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<UserResponse> update(@PathVariable("id") UUID id, @RequestBody CreateOrUpdateUserRequest req) {
        return ok(of(userService.updateUser(id, req.name(), req.email())));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("")
    @Override
    public ResponseEntity<Page<UserResponse>> list(Pageable pageable) {
        Page<UserResponse> page = userService.listUsers(pageable).map(UserResponse::of);
        return ok(page);
    }
}
