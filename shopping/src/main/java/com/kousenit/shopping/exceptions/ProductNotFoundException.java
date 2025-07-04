package com.kousenit.shopping.exceptions;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
    private final Long productId;
    
    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
        this.productId = productId;
    }
    
    public ProductNotFoundException(String message) {
        super(message);
        this.productId = null;
    }

}