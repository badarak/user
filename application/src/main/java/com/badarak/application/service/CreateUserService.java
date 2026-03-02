package com.badarak.application.service;

import com.badarak.domain.exception.UserAlreadyExistsException;
import com.badarak.domain.model.Email;
import com.badarak.domain.model.User;
import com.badarak.domain.model.UserId;
import com.badarak.domain.model.UserName;
import com.badarak.domain.port.in.CreateUserUseCase;
import com.badarak.domain.port.out.DomainEventPublisher;
import com.badarak.domain.port.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@Transactional
public class CreateUserService implements CreateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateUserService.class);

    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    CreateUserService(UserRepository userRepository, DomainEventPublisher eventPublisher) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public UserId execute(CreateUserCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        log.info("Creating user with email: {}", command.email());

        final var email = new Email(command.email());
        final var name = new UserName(command.firstName(), command.lastName());

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        final var user = User.create(email, name);

        userRepository.save(user);

        log.info("User created successfully: id={}, email={}", user.getId(), email);

        user.domainEvents().forEach(eventPublisher::publish);
        user.clearDomainEvents();
        return user.getId();
    }
}