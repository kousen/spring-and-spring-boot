package com.kousenit.shopping.controllers;

import com.kousenit.shopping.dto.ApiError;
import com.kousenit.shopping.entities.Product;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private GlobalExceptionHandler handler;

    @Autowired
    private Validator validator;

    @Test
    void constraintViolationsMapToValidationErrors() {
        var violations = validator.validate(new Product()); // empty product violates several constraints
        var exception = new ConstraintViolationException(new HashSet<>(violations));
        var request = new MockHttpServletRequest("POST", "/api/v1/products");

        ResponseEntity<ApiError> response =
                handler.handleConstraintViolationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.validationErrors())
                .isNotEmpty()
                .allSatisfy(error -> assertThat(error.field()).isNotBlank());
        assertThat(body.path()).isEqualTo("/api/v1/products");
    }
}
