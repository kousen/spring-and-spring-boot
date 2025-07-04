package com.kousenit.shopping.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kousenit.shopping.dto.ProductRequest;
import com.kousenit.shopping.dto.ProductResponse;
import com.kousenit.shopping.dto.StockUpdateRequest;
import com.kousenit.shopping.exceptions.InsufficientStockException;
import com.kousenit.shopping.exceptions.ProductNotFoundException;
import com.kousenit.shopping.services.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductRestController.class)
@ActiveProfiles("test")
class ProductRestControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private ProductService productService;
    
    private ProductResponse createSampleProductResponse() {
        return new ProductResponse(
            1L,
            "Test Product",
            new BigDecimal("99.99"),
            "Test Description",
            10,
            "TST-123456",
            "test@example.com",
            LocalDateTime.now(),
            LocalDateTime.now(),
            true,
            "IN_STOCK"
        );
    }
    
    private ProductRequest createSampleProductRequest() {
        return new ProductRequest(
            "Test Product",
            new BigDecimal("99.99"),
            "Test Description",
            10,
            "TST-123456",
            "test@example.com"
        );
    }
    
    @Test
    @DisplayName("Should get product by id successfully")
    void testGetProductById() throws Exception {
        // Given
        ProductResponse product = createSampleProductResponse();
        when(productService.getProductById(1L)).thenReturn(product);
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Product"))
            .andExpect(jsonPath("$.price").value(99.99))
            .andExpect(jsonPath("$.sku").value("TST-123456"));
    }
    
    @Test
    @DisplayName("Should return 404 when product not found")
    void testGetProductByIdNotFound() throws Exception {
        // Given
        when(productService.getProductById(999L))
            .thenThrow(new ProductNotFoundException(999L));
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/999"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/product-not-found"))
            .andExpect(jsonPath("$.title").value("Product Not Found"))
            .andExpect(jsonPath("$.productId").value(999));
    }
    
    @Test
    @DisplayName("Should get all products with pagination")
    void testGetAllProducts() throws Exception {
        // Given
        ProductResponse product = createSampleProductResponse();
        Page<ProductResponse> page = new PageImpl<>(
            List.of(product), 
            PageRequest.of(0, 20), 
            1
        );
        when(productService.getAllProducts(any())).thenReturn(page);
        
        // When/Then
        mockMvc.perform(get("/api/v1/products"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].name").value("Test Product"))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.number").value(0));
    }
    
    @Test
    @DisplayName("Should search products by name")
    void testSearchProducts() throws Exception {
        // Given
        ProductResponse product = createSampleProductResponse();
        when(productService.searchProductsByName("Test")).thenReturn(List.of(product));
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/search?name=Test"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Test Product"));
    }
    
    @Test
    @DisplayName("Should get products by price range")
    void testGetProductsByPriceRange() throws Exception {
        // Given
        ProductResponse product = createSampleProductResponse();
        when(productService.getProductsByPriceRange(any(), any())).thenReturn(List.of(product));
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/price-range?minPrice=50&maxPrice=150"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].price").value(99.99));
    }
    
    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct() throws Exception {
        // Given
        ProductRequest request = createSampleProductRequest();
        ProductResponse response = createSampleProductResponse();
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(response);
        
        // When/Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Product"))
            .andExpect(jsonPath("$.sku").value("TST-123456"));
    }
    
    @Test
    @DisplayName("Should return validation errors for invalid product")
    void testCreateProductValidationErrors() throws Exception {
        // Given - invalid product request
        ProductRequest invalidRequest = new ProductRequest(
            "", // blank name
            new BigDecimal("-10.00"), // negative price
            "A".repeat(501), // too long description
            -5, // negative quantity
            "INVALID-SKU", // invalid SKU pattern
            "not-an-email" // invalid email
        );
        
        // When/Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."));
    }
    
    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct() throws Exception {
        // Given
        ProductRequest request = createSampleProductRequest();
        ProductResponse response = createSampleProductResponse();
        when(productService.updateProduct(anyLong(), any(ProductRequest.class))).thenReturn(response);
        
        // When/Then
        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Product"));
    }
    
    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/v1/products/1"))
            .andExpect(status().isNoContent());
    }
    
    @Test
    @DisplayName("Should update stock successfully")
    void testUpdateStock() throws Exception {
        // Given
        StockUpdateRequest request = new StockUpdateRequest(50);
        ProductResponse response = createSampleProductResponse();
        when(productService.updateStock(anyLong(), any(Integer.class))).thenReturn(response);
        
        // When/Then
        mockMvc.perform(put("/api/v1/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    @DisplayName("Should reserve stock successfully")
    void testReserveStock() throws Exception {
        // Given
        StockUpdateRequest request = new StockUpdateRequest(5);
        ProductResponse response = createSampleProductResponse();
        when(productService.reserveStock(anyLong(), any(Integer.class))).thenReturn(response);
        
        // When/Then
        mockMvc.perform(post("/api/v1/products/1/reserve-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    @DisplayName("Should return bad request for insufficient stock")
    void testReserveStockInsufficientStock() throws Exception {
        // Given
        StockUpdateRequest request = new StockUpdateRequest(50);
        when(productService.reserveStock(anyLong(), any(Integer.class)))
            .thenThrow(new InsufficientStockException(1L, 50, 10));
        
        // When/Then
        mockMvc.perform(post("/api/v1/products/1/reserve-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/insufficient-stock"))
            .andExpect(jsonPath("$.title").value("Insufficient Stock"))
            .andExpect(jsonPath("$.productId").value(1))
            .andExpect(jsonPath("$.requestedQuantity").value(50))
            .andExpect(jsonPath("$.availableQuantity").value(10));
    }
    
    @Test
    @DisplayName("Should add stock successfully")
    void testAddStock() throws Exception {
        // Given
        StockUpdateRequest request = new StockUpdateRequest(10);
        ProductResponse response = createSampleProductResponse();
        when(productService.addStock(anyLong(), any(Integer.class))).thenReturn(response);
        
        // When/Then
        mockMvc.perform(post("/api/v1/products/1/add-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    @DisplayName("Should get low stock products")
    void testGetLowStockProducts() throws Exception {
        // Given
        ProductResponse product = createSampleProductResponse();
        when(productService.getLowStockProducts(any(Integer.class))).thenReturn(List.of(product));
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/low-stock?threshold=5"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)));
    }
    
    @Test
    @DisplayName("Should get expensive products")
    void testGetExpensiveProducts() throws Exception {
        // Given
        ProductResponse product = createSampleProductResponse();
        when(productService.getExpensiveProducts(any(BigDecimal.class))).thenReturn(List.of(product));
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/expensive?minPrice=50"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)));
    }
    
    @Test
    @DisplayName("Should return bad request for invalid type mismatch")
    void testInvalidPathVariable() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/products/invalid-id"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400));
    }
    
    @Test
    @DisplayName("Should return bad request for missing required parameter")
    void testMissingRequiredParameter() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/products/price-range?minPrice=100"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400));
    }
}