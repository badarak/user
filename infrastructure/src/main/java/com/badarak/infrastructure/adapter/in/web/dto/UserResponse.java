package com.badarak.infrastructure.adapter.in.web.dto;

import com.badarak.domain.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(

        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        String email,
        String firstName,
        String lastName,
        @Schema(example = "ACTIVE")
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
