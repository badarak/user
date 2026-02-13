package com.badarak.domain.exception;

import com.badarak.domain.model.Email;

import static java.lang.String.format;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(Email email) {
        super(ErrorCode.USER_ALREADY_EXISTS.name(), format("A user with email %s already exists", email.value()));
    }
}
