package com.pokedexsocial.backend.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/** Global handler for REST exceptions. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildErrorResponse(HttpStatus status, String error, Object details) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", error,
                "details", details
        );
    }

    /**
     * Handler for Exception {@link NotFoundException}
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()));
    }

    /**
     * Handler for Exception {@link MethodArgumentNotValidException}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors));
    }

    /**
     * Handler for Exception {@link ConstraintViolationException}
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations().stream()
                .map(v -> Map.of("param", v.getPropertyPath().toString(), "message", v.getMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, "Constraint Violation", errors));
    }

    /**
     * Handler for Generic Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage()));
    }
}
