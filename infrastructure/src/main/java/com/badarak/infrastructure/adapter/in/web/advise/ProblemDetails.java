package com.badarak.infrastructure.adapter.in.web.advise;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

public final class ProblemDetails {
    private static final String PROBLEM_BASE_URI = "https://badarak.com/problems";

    private ProblemDetails() {
    }

    public static ProblemDetail problem(HttpStatus httpStatus, String type, String title, String detail) {
        final var problemDetail = ProblemDetail.forStatus(httpStatus);
        problemDetail.setType(URI.create(PROBLEM_BASE_URI + "/" + type));
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
