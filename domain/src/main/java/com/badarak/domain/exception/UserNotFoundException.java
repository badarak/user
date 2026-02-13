package com.badarak.domain.exception;

import com.badarak.domain.model.UserId;

import static java.lang.String.format;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(UserId id) {
        super(ErrorCode.USER_NOT_FOUND.name(), format("User not found with id: %s", id.value()));
    }
}
