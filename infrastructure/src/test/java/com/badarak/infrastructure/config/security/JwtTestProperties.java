package com.badarak.infrastructure.config.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

@TestConfiguration(proxyBeanMethods = false)
public class JwtTestProperties {

    @Bean
    DynamicPropertyRegistrar jwtPropertiesRegistrar() {
        return registry -> {
            registry.add("security.jwt.public-key", JwtTestFactory::publicKeyBase64);
            registry.add("security.jwt.issuer", () -> JwtTestFactory.ISSUER);
            registry.add("security.jwt.audience", () -> JwtTestFactory.AUDIENCE);
        };
    }
}
