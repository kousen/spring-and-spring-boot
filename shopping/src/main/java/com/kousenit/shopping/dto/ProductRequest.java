package com.kousenit.shopping.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    String name,
    
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must be less than 1,000,000")
    @NotNull(message = "Price is required")
    BigDecimal price,
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,
    
    @Min(value = 0, message = "Quantity cannot be negative")
    @NotNull(message = "Quantity is required")
    Integer quantity,
    
    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z]{3}-[0-9]{6}$", 
             message = "SKU must follow the pattern: 3 uppercase letters, hyphen, 6 digits (e.g., ABC-123456)")
    String sku,
    
    @Email(message = "Contact email must be a valid email address")
    String contactEmail
) {}