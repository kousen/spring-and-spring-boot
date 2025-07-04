package com.kousenit.shopping.controllers;

import com.kousenit.shopping.dto.ApiError;
import com.kousenit.shopping.dto.ValidationError;
import com.kousenit.shopping.exceptions.InsufficientStockException;
import com.kousenit.shopping.exceptions.ProductNotFoundException;
import com.kousenit.shopping.exceptions.ProductValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String VALIDATION_FAILED = "Validation failed";
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleProductNotFoundException(
            ProductNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("Product not found: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setType(URI.create("https://api.shopping.com/problems/product-not-found"));
        problemDetail.setTitle("Product Not Found");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        if (ex.getProductId() != null) {
            problemDetail.setProperty("productId", ex.getProductId());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientStockException(
            InsufficientStockException ex, HttpServletRequest request) {
        
        logger.warn("Insufficient stock: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://api.shopping.com/problems/insufficient-stock"));
        problemDetail.setTitle("Insufficient Stock");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("productId", ex.getProductId());
        problemDetail.setProperty("requestedQuantity", ex.getRequestedQuantity());
        problemDetail.setProperty("availableQuantity", ex.getAvailableQuantity());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    @ExceptionHandler(ProductValidationException.class)
    public ResponseEntity<ProblemDetail> handleProductValidationException(
            ProductValidationException ex, HttpServletRequest request) {
        
        logger.warn("Product validation error: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://api.shopping.com/problems/validation-error"));
        problemDetail.setTitle("Validation Error");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        if (ex.getField() != null) {
            problemDetail.setProperty("field", ex.getField());
            problemDetail.setProperty("rejectedValue", ex.getValue());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("Validation failed for request: {}", ex.getMessage());
        
        List<ValidationError> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::mapFieldError)
            .collect(Collectors.toList());
        
        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            validationErrors.add(ValidationError.of(
                error.getObjectName(),
                null,
                error.getDefaultMessage(),
                error.getCode()
            ));
        });
        
        ApiError apiError = ApiError.withValidationErrors(
            HttpStatus.BAD_REQUEST,
            VALIDATION_FAILED,
            request.getRequestURI(),
            validationErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        logger.warn("Constraint violation: {}", ex.getMessage());
        
        List<ValidationError> validationErrors = ex.getConstraintViolations()
            .stream()
            .map(this::mapConstraintViolation)
            .collect(Collectors.toList());
        
        ApiError apiError = ApiError.withValidationErrors(
            HttpStatus.BAD_REQUEST,
            VALIDATION_FAILED,
            request.getRequestURI(),
            validationErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        logger.warn("Malformed JSON request: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Malformed JSON request");
        problemDetail.setType(URI.create("https://api.shopping.com/problems/malformed-request"));
        problemDetail.setTitle("Malformed Request");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logger.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
            ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, message);
        problemDetail.setType(URI.create("https://api.shopping.com/problems/type-mismatch"));
        problemDetail.setTitle("Type Mismatch");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("parameter", ex.getName());
        problemDetail.setProperty("rejectedValue", ex.getValue());
        problemDetail.setProperty("expectedType", ex.getRequiredType().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        logger.warn("Missing required parameter: {}", ex.getParameterName());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, message);
        problemDetail.setType(URI.create("https://api.shopping.com/problems/missing-parameter"));
        problemDetail.setTitle("Missing Parameter");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("parameter", ex.getParameterName());
        problemDetail.setProperty("parameterType", ex.getParameterType());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, "The requested resource was not found");
        problemDetail.setType(URI.create("https://api.shopping.com/problems/resource-not-found"));
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data integrity violation. This operation conflicts with existing data constraints.";
        
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Unique index or primary key violation")) {
                message = "A record with this information already exists.";
            } else if (ex.getMessage().contains("constraint")) {
                message = "This operation violates a data constraint.";
            }
        }
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, message);
        problemDetail.setType(URI.create("https://api.shopping.com/problems/data-integrity"));
        problemDetail.setTitle("Data Integrity Violation");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://api.shopping.com/problems/illegal-argument"));
        problemDetail.setTitle("Illegal Argument");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "An unexpected error occurred. Please try again later.");
        problemDetail.setType(URI.create("https://api.shopping.com/problems/internal-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        if (logger.isDebugEnabled()) {
            problemDetail.setProperty("debugMessage", ex.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
    
    private ValidationError mapFieldError(FieldError fieldError) {
        return ValidationError.of(
            fieldError.getField(),
            fieldError.getRejectedValue(),
            fieldError.getDefaultMessage(),
            fieldError.getCode()
        );
    }
    
    private ValidationError mapConstraintViolation(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        return ValidationError.of(
            field,
            violation.getInvalidValue(),
            violation.getMessage(),
            null
        );
    }
}