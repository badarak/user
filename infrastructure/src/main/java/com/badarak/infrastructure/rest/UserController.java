package com.badarak.infrastructure.rest;

import com.badarak.domain.model.User;
import com.badarak.domain.port.in.UserServicePort;
import com.badarak.domain.port.out.UserRepositoryPort;
import com.badarak.domain.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserServicePort userService;

    public UserController(UserRepositoryPort repository) {
        this.userService = new UserService(repository);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable("id") UUID id) {
        return ok(userService.findById(id));
    }

    @PostMapping("")
    public ResponseEntity<User> create(@RequestBody User user) {
        return status(HttpStatus.CREATED)
                .body(userService.createUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable("id") UUID id) {
        return ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable("id") UUID id, @RequestBody CreateUserRequest req) {
        return ok(userService.updateUser(id, req.name, req.email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    public record CreateUserRequest(String name, String email) {
    }

}
