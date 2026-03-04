package com.badarak.domain.port.in;

import com.badarak.domain.model.User;
import com.badarak.domain.model.UserId;

import static java.lang.String.format;

public interface UpdateUserUseCase {
    User execute(UpdateUserCommand command);

    record UpdateUserCommand(UserId userId, String firstName, String lastName) {

        public UpdateUserCommand {
            if (userId == null) throw new IllegalArgumentException("userId must not be null");
            requireNonBlank(firstName, "firstName");
            requireNonBlank(lastName,  "lastName");
        }

        private static void requireNonBlank(String value, String field) {
            if (value == null || value.isBlank())
                throw new IllegalArgumentException(format("UpdateUserCommand. %s must not be blank",  field));
        }
    }
}
