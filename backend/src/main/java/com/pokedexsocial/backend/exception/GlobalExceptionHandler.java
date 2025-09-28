package com.pokedexsocial.backend.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.util.Map;

/**
 * Global exception handler that converts exceptions into standardized
 * RFC 7807 {@link org.springframework.http.ProblemDetail} responses.
 *
 * <p>This handler ensures that all errors returned by the API are consistent,
 * including authentication, authorization, validation, and domain-specific errors.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ProblemDetail buildProblemDetail(
            HttpStatus status, String title, String detail, String typeUri, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setType(URI.create(typeUri));
        problem.setProperty("instance", request.getDescription(false).replace("uri=", ""));
        return problem;
    }

    /** Handles resource not found errors (404) */
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, WebRequest request) {
        return buildProblemDetail(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage(),
                "https://example.com/probs/not-found",
                request
        );
    }

    /** Handles forbidden access errors (403) */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return buildProblemDetail(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                ex.getMessage(),
                "https://example.com/probs/forbidden",
                request
        );
    }

    /** Handles invalid credentials or authentication errors (401) */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        return buildProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                ex.getMessage(),
                "https://example.com/probs/invalid-credentials",
                request
        );
    }

    /** Handles email or username conflicts (409) */
    @ExceptionHandler({EmailAlreadyUsedException.class, UsernameAlreadyUsedException.class})
    public ProblemDetail handleConflict(RuntimeException ex, WebRequest request) {
        return buildProblemDetail(
                HttpStatus.CONFLICT,
                "Conflict",
                ex.getMessage(),
                "https://example.com/probs/conflict",
                request
        );
    }

    /** Handles validation errors (400) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
                .toList();

        ProblemDetail problem = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Invalid request payload",
                "https://example.com/probs/validation-error",
                request
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    /** Handles constraint violations (400) */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        var errors = ex.getConstraintViolations().stream()
                .map(v -> Map.of("param", v.getPropertyPath().toString(), "message", v.getMessage()))
                .toList();

        ProblemDetail problem = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Constraint Violation",
                "Invalid request parameters",
                "https://example.com/probs/constraint-violation",
                request
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    /** Handles generic unexpected exceptions (500) */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, WebRequest request) {
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ex.getMessage(),
                "https://example.com/probs/internal-error",
                request
        );
    }

    /** Handles invalid user operations (400) */
    @ExceptionHandler(InvalidUserOperationException.class)
    public ProblemDetail handleInvalidUserOperation(InvalidUserOperationException ex, WebRequest request) {
        return buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid User Operation",
                ex.getMessage(),
                "https://example.com/probs/invalid-user-operation",
                request
        );
    }

    /** Handles errors during the genetic optimization process */
    @ExceptionHandler(CloneNotSupportedException.class)
    public ProblemDetail handleCloneNotSupported(CloneNotSupportedException ex, WebRequest request) {
        return buildProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Genetic Algorithm Error",
                ex.getMessage(),
                "https://example.com/probs/genetic-error",
                request
        );
    }
}
