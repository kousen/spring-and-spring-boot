package com.kousenit.shopping.exceptions;

import lombok.Getter;

@Getter
public class ProductValidationException extends RuntimeException {
    private final String field;
    private final Object value;
    
    public ProductValidationException(String field, Object value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }
}