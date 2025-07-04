package com.kousenit.shopping.entities;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductTest {
    
    @Autowired
    private Validator validator;
    
    @Test
    @DisplayName("Should create valid product with all required fields")
    void testValidProduct() {
        Product product = createValidProduct();
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).isEmpty();
    }
    
    @Test
    @DisplayName("Should fail validation when name is blank")
    void testInvalidProductBlankName() {
        Product product = createValidProduct();
        product.setName("");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(2); // Both @NotBlank and @Size are triggered
        assertThat(violations)
            .extracting(ConstraintViolation::getMessage)
            .containsExactlyInAnyOrder(
                "Product name is required",
                "Product name must be between 3 and 100 characters"
            );
    }
    
    @Test
    @DisplayName("Should fail validation when price is negative")
    void testInvalidProductNegativePrice() {
        Product product = createValidProduct();
        product.setPrice(new BigDecimal("-10.00"));
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Price must be greater than 0");
    }
    
    @Test
    @DisplayName("Should fail validation when SKU pattern is invalid")
    void testInvalidProductSKUPattern() {
        Product product = createValidProduct();
        product.setSku("INVALID-SKU");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .contains("SKU must follow the pattern");
    }
    
    @Test
    @DisplayName("Should fail validation when email is invalid")
    void testInvalidProductEmail() {
        Product product = createValidProduct();
        product.setContactEmail("not-an-email");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Contact email must be a valid email address");
    }
    
    @Test
    @DisplayName("Should correctly check stock availability")
    void testHasStock() {
        Product product = createValidProduct();
        product.setQuantity(10);
        
        assertTrue(product.hasStock(5));
        assertTrue(product.hasStock(10));
        assertFalse(product.hasStock(11));
    }
    
    @Test
    @DisplayName("Should decrement stock successfully")
    void testDecrementStock() {
        Product product = createValidProduct();
        product.setQuantity(10);
        
        product.decrementStock(3);
        
        assertEquals(7, product.getQuantity());
    }
    
    @Test
    @DisplayName("Should throw exception when decrementing more than available stock")
    void testDecrementStockInsufficientQuantity() {
        Product product = createValidProduct();
        product.setQuantity(5);
        
        assertThrows(IllegalArgumentException.class, () -> product.decrementStock(10));
        assertEquals(5, product.getQuantity());
    }
    
    @Test
    @DisplayName("Should increment stock successfully")
    void testIncrementStock() {
        Product product = createValidProduct();
        product.setQuantity(10);
        
        product.incrementStock(5);
        
        assertEquals(15, product.getQuantity());
    }
    
    @Test
    @DisplayName("Should set timestamps on persist")
    void testOnCreate() {
        Product product = createValidProduct();
        assertNull(product.getCreatedAt());
        assertNull(product.getUpdatedAt());
        
        product.onCreate();
        
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
        // Timestamps should be very close, but not necessarily equal due to nanosecond precision
        assertThat(product.getCreatedAt()).isCloseTo(product.getUpdatedAt(), 
            org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.SECONDS));
    }
    
    @Test
    @DisplayName("Should update timestamp on update")
    void testOnUpdate() {
        Product product = createValidProduct();
        product.onCreate();
        
        var originalCreatedAt = product.getCreatedAt();
        var originalUpdatedAt = product.getUpdatedAt();
        
        // Simulate time passing
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        product.onUpdate();
        
        assertEquals(originalCreatedAt, product.getCreatedAt());
        assertNotEquals(originalUpdatedAt, product.getUpdatedAt());
    }
    
    private Product createValidProduct() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setDescription("A test product");
        product.setQuantity(10);
        product.setSku("TST-123456");
        product.setContactEmail("test@example.com");
        return product;
    }
}