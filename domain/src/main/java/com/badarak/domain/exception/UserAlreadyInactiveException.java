package com.badarak.domain.exception;

import com.badarak.domain.model.UserId;

import static java.lang.String.format;

public class UserAlreadyInactiveException extends DomainException {
    public UserAlreadyInactiveException(UserId id) {
        super(ErrorCode.USER_ALREADY_INACTIVE.name(), format("User %s is already inactive", id.value()));
    }
}