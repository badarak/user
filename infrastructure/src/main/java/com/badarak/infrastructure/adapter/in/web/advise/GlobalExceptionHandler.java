package com.badarak.infrastructure.adapter.in.web.advise;

import com.badarak.domain.exception.DomainException;
import com.badarak.domain.exception.UserAlreadyExistsException;
import com.badarak.domain.exception.UserAlreadyInactiveException;
import com.badarak.domain.exception.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> onValidation(MethodArgumentNotValidException ex) {
        final List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"))
                .toList();
        final var p = problem(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                "One or more fields are invalid"
        );
        p.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(p);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> onConstraintViolation(ConstraintViolationException ex) {
        final List<Map<String, String>> errors = ex.getConstraintViolations()
                .stream()
                .map(cv -> Map.of(
                        "field", cv.getPropertyPath().toString(),
                        "message", cv.getMessage()))
                .toList();
        final var problemDetail = problem(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                "One or more parameters are invalid");
        problemDetail.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ProblemDetail> onNotFound(UserNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(domainProblem(HttpStatus.NOT_FOUND, "User Not Found", ex));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    ResponseEntity<ProblemDetail> onConflict(UserAlreadyExistsException ex) {
        log.warn("Conflict (duplicate email): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(domainProblem(HttpStatus.CONFLICT, "User Already Exists", ex));
    }

    @ExceptionHandler(UserAlreadyInactiveException.class)
    ResponseEntity<ProblemDetail> onAlreadyInactive(UserAlreadyInactiveException ex) {
        log.warn("Conflict (already inactive): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(domainProblem(HttpStatus.CONFLICT, "User Already Inactive", ex));
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ProblemDetail> onDomain(DomainException ex) {
        log.warn("Domain error [{}]: {}", ex.errorCode(), ex.getMessage());
        return ResponseEntity.badRequest()
                .body(domainProblem(HttpStatus.BAD_REQUEST, "Domain Error", ex));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> onUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError()
                .body(problem(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error", "An unexpected error occurred."));
    }

    private static ProblemDetail domainProblem(HttpStatus httpStatus, String title, DomainException ex) {
        final var problemDetail = problem(httpStatus, title, ex.getMessage());
        problemDetail.setProperty("errorCode", ex.errorCode());
        return problemDetail;
    }

    private static ProblemDetail problem(HttpStatus httpStatus, String title, String detail) {
        final var problemDetail = ProblemDetail.forStatus(httpStatus);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

}
