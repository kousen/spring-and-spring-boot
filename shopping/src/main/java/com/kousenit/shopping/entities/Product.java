package com.kousenit.shopping.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku", unique = true),
    @Index(name = "idx_product_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    @Column(nullable = false)
    private String name;
    
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must be less than 1,000,000")
    @NotNull(message = "Price is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;
    
    @Min(value = 0, message = "Quantity cannot be negative")
    @NotNull(message = "Quantity is required")
    @Column(nullable = false)
    private Integer quantity;
    
    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z]{3}-[0-9]{6}$", 
             message = "SKU must follow the pattern: 3 uppercase letters, hyphen, 6 digits (e.g., ABC-123456)")
    @Column(unique = true, nullable = false)
    private String sku;
    
    @Email(message = "Contact email must be a valid email address")
    private String contactEmail;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean hasStock(int requestedQuantity) {
        return this.quantity >= requestedQuantity;
    }
    
    public void decrementStock(int amount) {
        if (!hasStock(amount)) {
            throw new IllegalArgumentException(
                String.format("Cannot decrement stock by %d. Only %d available", amount, this.quantity)
            );
        }
        this.quantity -= amount;
    }
    
    public void incrementStock(int amount) {
        this.quantity += amount;
    }
}