package com.badarak.domain.exception;

public class InvalidUserNameException extends DomainException {
    public InvalidUserNameException(String message) {
        super(ErrorCode.USER_INVALID_NAME.name(), message);
    }
}
