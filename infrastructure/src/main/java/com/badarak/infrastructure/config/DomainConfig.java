package com.badarak.infrastructure.config;

import com.badarak.domain.port.in.UserEventPublisher;
import com.badarak.domain.port.in.UserServicePort;
import com.badarak.domain.port.out.UserRepositoryPort;
import com.badarak.domain.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public UserServicePort userService(UserRepositoryPort repository, UserEventPublisher eventPublisher) {
        return new UserService(repository, eventPublisher);
    }
}
