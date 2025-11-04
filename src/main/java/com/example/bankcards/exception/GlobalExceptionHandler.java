package com.example.bankcards.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DefaultErrorMessage> handleException(Exception ex, HttpServletRequest request) {
        log.error("[ERROR] Unhandled exception", ex);

        return getResponseEntity("Internal Server Error",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                null,
                "INTERNAL_SERVER_ERROR"
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<DefaultErrorMessage> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request) {

        log.warn("[WARN] Entity not found: {}", ex.getMessage());

        return getResponseEntity(
                "Entity not found",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                null,
                "ENTITY_NOT_FOUND"
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<DefaultErrorMessage> handleEntityNotFoundException(
            ApiException ex,
            HttpServletRequest request) {

        log.warn("[WARN] API Exception: [{}] {} (path: {})",
                ex.getErrorCode(), ex.getMessage(), request.getRequestURI());

        return getResponseEntity(
                "API error",
                ex.getMessage(),
                ex.getHttpStatus(),
                request.getRequestURI(),
                null,
                ex.getErrorCode()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DefaultErrorMessage> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<DefaultErrorMessage.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new DefaultErrorMessage.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        return getResponseEntity(
                "Validation Failed",
                "One or more fields are invalid",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                fieldErrors,
                "VALIDATION_ERROR"
        );
    }

    private ResponseEntity<DefaultErrorMessage> getResponseEntity(String title,
                                                                  String detail,
                                                                  int status,
                                                                  String instance,
                                                                  List<DefaultErrorMessage.FieldError> fieldErrors,
                                                                  String errorCode) {
        var defaultErrorMessage = DefaultErrorMessage.builder()
                .title(title)
                .detail(detail)
                .status(status)
                .timestamp(Instant.now())
                .instance(instance)
                .errorCode(errorCode)
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(status).body(defaultErrorMessage);
    }
}
