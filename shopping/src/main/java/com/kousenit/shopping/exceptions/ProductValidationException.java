package com.kousenit.shopping.exceptions;

public class ProductValidationException extends RuntimeException {
    private final String field;
    private final Object value;
    
    public ProductValidationException(String field, Object value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }
    
    public String getField() {
        return field;
    }
    
    public Object getValue() {
        return value;
    }
}