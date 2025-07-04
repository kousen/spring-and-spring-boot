package com.kousenit.shopping.dto;

import com.kousenit.shopping.entities.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long id,
    String name,
    BigDecimal price,
    String description,
    Integer quantity,
    String sku,
    String contactEmail,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean inStock,
    String stockStatus
) {
    
    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getDescription(),
            product.getQuantity(),
            product.getSku(),
            product.getContactEmail(),
            product.getCreatedAt(),
            product.getUpdatedAt(),
            product.getQuantity() > 0,
            getStockStatus(product.getQuantity())
        );
    }
    
    private static String getStockStatus(Integer quantity) {
        if (quantity == 0) return "OUT_OF_STOCK";
        if (quantity < 10) return "LOW_STOCK";
        if (quantity < 50) return "MEDIUM_STOCK";
        return "IN_STOCK";
    }
}