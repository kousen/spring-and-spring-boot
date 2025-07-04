package com.kousenit.shopping.exceptions;

public class InsufficientStockException extends RuntimeException {
    private final Long productId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;
    
    public InsufficientStockException(Long productId, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("Insufficient stock for product %d. Requested: %d, Available: %d", 
              productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}