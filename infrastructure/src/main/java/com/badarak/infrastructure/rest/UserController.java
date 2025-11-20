package com.badarak.infrastructure.rest;

import com.badarak.domain.model.User;
import com.badarak.domain.port.in.UserServicePort;
import com.badarak.domain.port.out.UserRepositoryPort;
import com.badarak.domain.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/users")
public final class UserController implements UserControllerDocumentation {
    private final UserServicePort userService;

    public UserController(UserRepositoryPort repository) {
        this.userService = new UserService(repository);
    }

    @PostMapping("")
    @Override
    public ResponseEntity<User> create(@RequestBody CreateUserRequest req) {
        return status(HttpStatus.CREATED)
                .body(userService.createUser(req.name, req.email));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<User> get(@PathVariable("id") UUID id) {
        return ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<User> update(@PathVariable("id") UUID id, @RequestBody CreateUserRequest req) {
        return ok(userService.updateUser(id, req.name, req.email));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("")
    @Override
    public ResponseEntity<Page<User>> list(Pageable pageable) {
        return ok(userService.listUsers(pageable));
    }

    public record CreateUserRequest(String name, String email) {
    }

}
