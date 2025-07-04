package com.kousenit.shopping.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    HttpStatus status,
    int statusCode,
    String error,
    String message,
    String path,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp,
    List<ValidationError> validationErrors,
    Map<String, Object> details
) {
    
    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(
            status,
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            LocalDateTime.now(),
            null,
            null
        );
    }
    
    public static ApiError withValidationErrors(HttpStatus status, String message, 
                                               String path, List<ValidationError> validationErrors) {
        return new ApiError(
            status,
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            LocalDateTime.now(),
            validationErrors,
            null
        );
    }
    
    public static ApiError withDetails(HttpStatus status, String message, 
                                      String path, Map<String, Object> details) {
        return new ApiError(
            status,
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            LocalDateTime.now(),
            null,
            details
        );
    }
}