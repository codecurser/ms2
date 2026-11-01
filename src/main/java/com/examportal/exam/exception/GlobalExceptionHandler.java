package com.examportal.exam.exception;

import com.examportal.exam.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExamNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExamNotFoundException(
            ExamNotFoundException ex, HttpServletRequest request) {
        log.warn("Exam not found: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .error("EXAM_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UnauthorizedExamAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedExamAccessException(
            UnauthorizedExamAccessException ex, HttpServletRequest request) {
        // Return 404 to avoid exposing exam existence
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .error("EXAM_NOT_FOUND")
                .message("Exam not found")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidExamStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidExamStateException(
            InvalidExamStateException ex, HttpServletRequest request) {
        log.warn("Invalid exam state: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .error("INVALID_EXAM_STATE")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ShareCodeGenerationException.class)
    public ResponseEntity<ErrorResponse> handleShareCodeGenerationException(
            ShareCodeGenerationException ex, HttpServletRequest request) {
        log.error("Share code generation error: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .error("SHARE_CODE_GENERATION_FAILED")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed for request: {}", request.getRequestURI());
        List<ErrorResponse.ValidationError> details = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.add(new ErrorResponse.ValidationError(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        ErrorResponse response = ErrorResponse.builder()
                .error("VALIDATION_FAILED")
                .message("Request parameters validation failed")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .details(details)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({IllegalArgumentException.class, PropertyReferenceException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            Exception ex, HttpServletRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .error("BAD_REQUEST")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .error("ACCESS_DENIED")
                .message("Access is denied")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception: ", ex);
        ErrorResponse response = ErrorResponse.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
