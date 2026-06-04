package com.example.SuChefService.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "RESOURCE_NOT_FOUND");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "UNAUTHORIZED");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(SubscriptionLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleSubscriptionLimitExceededException(SubscriptionLimitExceededException ex) {
        log.warn("Subscription limit exceeded: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "SUBSCRIPTION_LIMIT_EXCEEDED");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(NoRestaurantException.class)
    public ResponseEntity<Map<String, String>> handleNoRestaurantException(NoRestaurantException ex) {
        log.warn("No restaurant for user: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "NO_RESTAURANT");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Authentication failed: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Authentication failed: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid",
                        (a, b) -> a
                ));
        Map<String, String> response = new HashMap<>();
        response.put("error", "VALIDATION_FAILED");
        response.put("message", "Input validation failed");
        response.putAll(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
