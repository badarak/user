package com.badarak.infrastructure.config.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("security.jwt")
public record JwtProperties(
        @NotBlank String publicKey,
        @NotBlank String issuer,
        @NotBlank String audience
) {
}
