package com.badarak.domain.exception;

public class InvalidEmailException extends DomainException {
    public InvalidEmailException(String message) {
        super(ErrorCode.INVALID_EMAIL.name() , message);
    }
}
