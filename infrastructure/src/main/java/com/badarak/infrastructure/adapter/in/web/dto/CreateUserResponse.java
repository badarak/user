package com.badarak.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record CreateUserResponse(
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id
) {
}


