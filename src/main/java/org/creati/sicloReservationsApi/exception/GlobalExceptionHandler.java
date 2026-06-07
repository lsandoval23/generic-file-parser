package org.creati.sicloReservationsApi.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.auth.dto.ErrorResponse;
import org.creati.sicloReservationsApi.auth.exception.DuplicateResourceException;
import org.creati.sicloReservationsApi.auth.exception.GenerationTokenException;
import org.creati.sicloReservationsApi.auth.exception.InvalidCredentialsException;
import org.creati.sicloReservationsApi.auth.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .code("AUTH_INVALID_CREDENTIALS")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(GenerationTokenException.class)
    public ResponseEntity<ErrorResponse> handleTokenError(
            GenerationTokenException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .code("AUTH_TOKEN_ERROR")
                .message("There was a problem creating the token")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(AuthorizationDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                .code("AUTHORIZATION_DENIED")
                .message("You do not have permission to access this resource")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build());

    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .code("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .code("DUPLICATE_RESOURCE")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error occurred", ex);

        ErrorResponse error = ErrorResponse.builder()
                .code("GENERIC_ERROR")
                .message("Unexpected error occurred")
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse(ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_FIELD_ERROR")
                .message(errorMessage)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse(ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_FIELD_ERROR")
                .message(errorMessage)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .code("ILLEGAL_ARGUMENT_ERROR")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }



}
