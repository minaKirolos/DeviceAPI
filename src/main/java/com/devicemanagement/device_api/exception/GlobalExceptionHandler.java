package com.devicemanagement.device_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           HttpServletRequest request, List<String> details) {

        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(DeviceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request,List.of());
    }
    @ExceptionHandler(DeviceInUseException.class)
    public ResponseEntity<ApiError> handleInUse(DeviceInUseException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed or invalid request body", request, List.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiError> handleInvalidSortProperty(PropertyReferenceException ex,
                                                              HttpServletRequest request) {
        String message = "Unknown sort/filter property '" + ex.getPropertyName()
                + "'. Allowed: id, name, brand, state, creationTime.";
        return build(HttpStatus.BAD_REQUEST, message, request, List.of());
    }
}
