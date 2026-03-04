package com.badarak.domain.model;

public record UserQuery(int page, int size, UserStatus status) {

    public UserQuery {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1) throw new IllegalArgumentException("size must be >= 1");
        if (size > 100) throw new IllegalArgumentException("size must be <= 100");
    }

    public static UserQuery defaultQuery() {
        return new UserQuery(0, 20, null);
    }
}