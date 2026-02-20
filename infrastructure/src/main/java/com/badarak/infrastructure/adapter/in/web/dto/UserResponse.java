package com.badarak.infrastructure.adapter.in.web.dto;

import com.badarak.domain.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(

        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(example = "john.doe@example.com")
        String email,
        @Schema(example = "John")
        String firstName,
        @Schema(example = "Doe")
        String lastName,
        @Schema(example = "ACTIVE")
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
