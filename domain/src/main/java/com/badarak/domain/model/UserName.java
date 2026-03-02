package com.badarak.domain.model;

import com.badarak.domain.exception.InvalidUserNameException;

import java.util.Objects;

import static java.lang.String.format;

public record UserName(String firstName, String lastName) {

    static final int MIN_LENGTH = 1;
    static final int MAX_LENGTH = 100;

    public UserName {
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");

        firstName = validatePart(firstName.strip(), "firstName");
        lastName = validatePart(lastName.strip(), "lastName");
    }

    private static String validatePart(String value, String field) {
        if (value.isBlank()) {
            throw new InvalidUserNameException(format("%s must not be blank", field));
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidUserNameException(
                    format("%s length must be between %d and %d, got: %d", field, MIN_LENGTH, MAX_LENGTH, value.length()));
        }
        return value;
    }


    public String fullName() {
        return format("%s %s", firstName, lastName);
    }

    @Override
    public String toString() {
        return fullName();
    }
}