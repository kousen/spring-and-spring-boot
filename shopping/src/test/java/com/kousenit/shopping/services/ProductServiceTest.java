package com.kousenit.shopping.services;

import com.kousenit.shopping.dto.ProductRequest;
import com.kousenit.shopping.dto.ProductResponse;
import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.exceptions.InsufficientStockException;
import com.kousenit.shopping.exceptions.ProductNotFoundException;
import com.kousenit.shopping.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceTest {
    
    @MockitoBean
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;
    
    private Product testProduct;
    private ProductRequest testProductRequest;
    
    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setDescription("Test Description");
        testProduct.setQuantity(10);
        testProduct.setSku("TST-123456");
        testProduct.setContactEmail("test@example.com");
        
        testProductRequest = new ProductRequest(
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
    void testGetProductByIdSuccess() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        // When
        ProductResponse result = productService.getProductById(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Product");
        assertThat(result.price()).isEqualTo(new BigDecimal("99.99"));
        verify(productRepository).findById(1L);
    }
    
    @Test
    @DisplayName("Should throw exception when product not found")
    void testGetProductByIdNotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThrows(ProductNotFoundException.class, 
            () -> productService.getProductById(999L));
        verify(productRepository).findById(999L);
    }
    
    @Test
    @DisplayName("Should get all products with pagination")
    void testGetAllProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(productPage);
        
        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Test Product");
        verify(productRepository).findAll(pageable);
    }
    
    @Test
    @DisplayName("Should search products by name")
    void testSearchProductsByName() {
        // Given
        when(productRepository.findByNameContainingIgnoreCase("Test"))
            .thenReturn(List.of(testProduct));
        
        // When
        List<ProductResponse> result = productService.searchProductsByName("Test");
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Test Product");
        verify(productRepository).findByNameContainingIgnoreCase("Test");
    }
    
    @Test
    @DisplayName("Should get products by price range")
    void testGetProductsByPriceRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("150.00");
        when(productRepository.findByPriceBetween(minPrice, maxPrice))
            .thenReturn(List.of(testProduct));
        
        // When
        List<ProductResponse> result = productService.getProductsByPriceRange(minPrice, maxPrice);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).price()).isEqualTo(new BigDecimal("99.99"));
        verify(productRepository).findByPriceBetween(minPrice, maxPrice);
    }
    
    @Test
    @DisplayName("Should throw exception when min price greater than max price")
    void testGetProductsByPriceRangeInvalidRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("150.00");
        BigDecimal maxPrice = new BigDecimal("50.00");
        
        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> productService.getProductsByPriceRange(minPrice, maxPrice));
        verify(productRepository, never()).findByPriceBetween(any(), any());
    }
    
    @Test
    @DisplayName("Should create product successfully")
    void testCreateProductSuccess() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // When
        ProductResponse result = productService.createProduct(testProductRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Product");
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProductSuccess() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        // No need to stub existsBySku since the SKU isn't changing
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // When
        ProductResponse result = productService.updateProduct(1L, testProductRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Product");
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProductSuccess() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        
        // When
        productService.deleteProduct(1L);
        
        // Then
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void testDeleteProductNotFound() {
        // Given
        when(productRepository.existsById(999L)).thenReturn(false);
        
        // When/Then
        assertThrows(ProductNotFoundException.class,
            () -> productService.deleteProduct(999L));
        verify(productRepository).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }
    
    @Test
    @DisplayName("Should update stock successfully")
    void testUpdateStockSuccess() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // When
        ProductResponse result = productService.updateStock(1L, 25);
        
        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should throw exception when updating stock with negative quantity")
    void testUpdateStockNegativeQuantity() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> productService.updateStock(1L, -5));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should reserve stock successfully")
    void testReserveStockSuccess() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // When
        ProductResponse result = productService.reserveStock(1L, 5);
        
        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should throw exception when reserving more stock than available")
    void testReserveStockInsufficientStock() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        // When/Then
        assertThrows(InsufficientStockException.class,
            () -> productService.reserveStock(1L, 15));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should add stock successfully")
    void testAddStockSuccess() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // When
        ProductResponse result = productService.addStock(1L, 10);
        
        // Then
        assertThat(result).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should throw exception when adding negative stock")
    void testAddStockNegativeQuantity() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> productService.addStock(1L, -5));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Should get low stock products")
    void testGetLowStockProducts() {
        // Given
        when(productRepository.findLowStockProducts(10)).thenReturn(List.of(testProduct));
        
        // When
        List<ProductResponse> result = productService.getLowStockProducts(10);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).quantity()).isEqualTo(10);
        verify(productRepository).findLowStockProducts(10);
    }
    
    @Test
    @DisplayName("Should get expensive products")
    void testGetExpensiveProducts() {
        // Given
        BigDecimal minPrice = new BigDecimal("50.00");
        when(productRepository.findExpensiveProducts(minPrice)).thenReturn(List.of(testProduct));
        
        // When
        List<ProductResponse> result = productService.getExpensiveProducts(minPrice);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).price()).isEqualTo(new BigDecimal("99.99"));
        verify(productRepository).findExpensiveProducts(minPrice);
    }
}