package com.badarak.domain.port.in;

import com.badarak.domain.model.UserId;

import static java.lang.String.format;

public interface CreateUserUseCase {

    UserId execute(CreateUserCommand command);

    record CreateUserCommand(
            String email,
            String firstName,
            String lastName
    ) {
        public CreateUserCommand {
            requireNonBlank(email, "email");
            requireNonBlank(firstName, "firstName");
            requireNonBlank(lastName, "lastName");
        }

        private static void requireNonBlank(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(format("CreateUserCommand.%s must not be null or blank", fieldName));
            }
        }
    }
}
