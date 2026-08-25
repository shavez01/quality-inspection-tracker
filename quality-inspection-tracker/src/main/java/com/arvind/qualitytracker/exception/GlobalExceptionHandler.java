package com.arvind.qualitytracker.exception;

import com.arvind.qualitytracker.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts application and validation errors into consistent API responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(
                new ApiError(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        errors
                )
        );
    }

    @ExceptionHandler(InspectionNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(InspectionNotFoundException exception) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InspectionAlreadyResolvedException.class)
    public ResponseEntity<ApiError> handleAlreadyResolved(InspectionAlreadyResolvedException exception) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception) {
        return buildError(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(
                new ApiError(LocalDateTime.now(), status.value(), message, Map.of())
        );
    }
}
