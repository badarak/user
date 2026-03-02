package com.badarak.domain.model;

import com.badarak.domain.exception.InvalidEmailException;

import java.util.Objects;
import java.util.regex.Pattern;

import static java.lang.String.format;


public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final int MAX_LENGTH = 254;

    public Email {
        Objects.requireNonNull(value, "Email value must not be null");
        final String trimmed = value.strip();
        if (trimmed.isBlank()) {
            throw new InvalidEmailException("Email must not be blank");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvalidEmailException(
                    format("Email must not exceed %d characters, got: %d", MAX_LENGTH, trimmed.length())
            );
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidEmailException(format("Invalid email format: %s", trimmed));
        }
        value = trimmed.toLowerCase();
    }

    @Override
    public String toString() {
        return value;
    }
}