package com.kousenit.shopping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kousenit.shopping.dto.ProductRequest;
import com.kousenit.shopping.dto.ProductResponse;
import com.kousenit.shopping.dto.StockUpdateRequest;
import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Shopping application.
 * 
 * Testing Strategy:
 * - Uses @DirtiesContext to refresh the Spring context after each test method
 * - This ensures complete test isolation when testing database constraints
 * - @BeforeEach clears the database before each test for clean state
 * - Allows real database commits to test constraint violations (e.g., duplicate SKU)
 * 
 * Alternative approaches considered:
 * - @Transactional + @Rollback(false): Caused Hibernate session conflicts
 * - Manual cleanup only: Less robust isolation between tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ShoppingApplicationIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProductRepository productRepository;
    
    @BeforeEach
    void setUp() {
        // Clear the database before each test
        productRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Should perform complete product lifecycle operations")
    void testCompleteProductLifecycle() throws Exception {
        // Verify database is empty (CommandLineRunner should not run in test profile)
        assertThat(productRepository.count()).isEqualTo(0);
        
        // Step 1: Create a product
        ProductRequest createRequest = new ProductRequest(
            "Integration Test Product",
            new BigDecimal("199.99"),
            "A product for integration testing",
            25,
            "INT-123456",
            "integration@example.com"
        );
        
        String createResponse = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.name").value("Integration Test Product"))
            .andExpect(jsonPath("$.price").value(199.99))
            .andExpect(jsonPath("$.quantity").value(25))
            .andExpect(jsonPath("$.sku").value("INT-123456"))
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        ProductResponse createdProduct = objectMapper.readValue(createResponse, ProductResponse.class);
        Long productId = createdProduct.id();
        
        // Verify database state
        assertThat(productRepository.count()).isEqualTo(1);
        
        // Step 2: Get the created product
        mockMvc.perform(get("/api/v1/products/" + productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("Integration Test Product"));
        
        // Step 3: Update the product
        ProductRequest updateRequest = new ProductRequest(
            "Updated Integration Product",
            new BigDecimal("249.99"),
            "Updated description",
            30,
            "INT-123456", // Same SKU
            "updated@example.com"
        );
        
        mockMvc.perform(put("/api/v1/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Integration Product"))
            .andExpect(jsonPath("$.price").value(249.99))
            .andExpect(jsonPath("$.quantity").value(30));
        
        // Step 4: Search for the product
        mockMvc.perform(get("/api/v1/products/search?name=Updated"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Updated Integration Product"));
        
        // Step 5: Update stock
        StockUpdateRequest stockUpdate = new StockUpdateRequest(50);
        
        mockMvc.perform(put("/api/v1/products/" + productId + "/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockUpdate)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(50));
        
        // Step 6: Reserve some stock
        StockUpdateRequest reserveRequest = new StockUpdateRequest(10);
        
        mockMvc.perform(post("/api/v1/products/" + productId + "/reserve-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(40));
        
        // Step 7: Add more stock
        StockUpdateRequest addRequest = new StockUpdateRequest(15);
        
        mockMvc.perform(post("/api/v1/products/" + productId + "/add-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(55));
        
        // Step 8: Get products in price range
        mockMvc.perform(get("/api/v1/products/price-range?minPrice=200&maxPrice=300"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].price").value(249.99));
        
        // Step 9: Get all products with pagination
        mockMvc.perform(get("/api/v1/products?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
        
        // Step 10: Delete the product
        mockMvc.perform(delete("/api/v1/products/" + productId))
            .andExpect(status().isNoContent());
        
        // Verify product is deleted
        mockMvc.perform(get("/api/v1/products/" + productId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/product-not-found"));
        
        // Verify database is empty again
        assertThat(productRepository.count()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should handle validation errors in integration context")
    void testValidationErrorsIntegration() throws Exception {
        // Create product with validation errors
        ProductRequest invalidRequest = new ProductRequest(
            "", // blank name
            new BigDecimal("0.00"), // zero price
            null, // null description is OK
            -1, // negative quantity
            "INVALID", // invalid SKU
            "not-email" // invalid email
        );
        
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."));
        
        // Verify no product was created
        assertThat(productRepository.count()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should handle insufficient stock scenarios")
    void testInsufficientStockIntegration() throws Exception {
        // Create a product with limited stock
        Product product = new Product();
        product.setName("Limited Stock Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setQuantity(5);
        product.setSku("LMT-123456");
        product.setContactEmail("test@example.com");
        
        Product saved = productRepository.save(product);
        
        // Try to reserve more stock than available
        StockUpdateRequest request = new StockUpdateRequest(10);
        
        mockMvc.perform(post("/api/v1/products/" + saved.getId() + "/reserve-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/insufficient-stock"))
            .andExpect(jsonPath("$.productId").value(saved.getId()))
            .andExpect(jsonPath("$.requestedQuantity").value(10))
            .andExpect(jsonPath("$.availableQuantity").value(5));
        
        // Verify stock wasn't changed
        Product unchanged = productRepository.findById(saved.getId()).orElseThrow();
        assertThat(unchanged.getQuantity()).isEqualTo(5);
    }
    
    @Test
    @DisplayName("Should handle duplicate SKU scenarios")
    void testDuplicateSkuIntegration() throws Exception {
        // Create first product
        Product existingProduct = new Product();
        existingProduct.setName("Existing Product");
        existingProduct.setPrice(new BigDecimal("99.99"));
        existingProduct.setQuantity(10);
        existingProduct.setSku("DUP-123456");
        existingProduct.setContactEmail("existing@example.com");
        
        productRepository.save(existingProduct);
        
        // Try to create second product with same SKU
        ProductRequest duplicateRequest = new ProductRequest(
            "Duplicate Product",
            new BigDecimal("149.99"),
            "Another product",
            5,
            "DUP-123456", // Same SKU
            "duplicate@example.com"
        );
        
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/duplicate-sku"))
            .andExpect(jsonPath("$.title").value("Duplicate SKU"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("A product with this SKU already exists."));
        
        // Verify only one product exists
        assertThat(productRepository.count()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should test CommandLineRunner does not execute in test profile")
    void testCommandLineRunnerDoesNotExecuteInTestProfile() {
        // Verify that no sample data was loaded (CommandLineRunner should be disabled in test profile)
        assertThat(productRepository.count()).isEqualTo(0);
    }
}