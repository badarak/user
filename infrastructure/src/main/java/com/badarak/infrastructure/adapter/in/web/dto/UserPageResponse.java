package com.badarak.infrastructure.adapter.in.web.dto;

import java.util.List;

public record UserPageResponse(
        List<UserResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}