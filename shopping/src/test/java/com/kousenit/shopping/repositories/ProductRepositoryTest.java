package com.kousenit.shopping.repositories;

import com.kousenit.shopping.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ProductRepository productRepository;
    
    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;
    
    @BeforeEach
    void setUp() {
        testProduct1 = createProduct("iPhone 15", new BigDecimal("999.99"), 50, "APP-000001");
        testProduct2 = createProduct("MacBook Pro", new BigDecimal("2499.99"), 10, "APP-000002");
        testProduct3 = createProduct("AirPods Pro", new BigDecimal("249.99"), 5, "APP-000003");
        
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testProduct3);
        entityManager.flush();
    }
    
    @Test
    @DisplayName("Should find product by SKU")
    void testFindBySku() {
        Optional<Product> found = productRepository.findBySku("APP-000001");
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("iPhone 15");
    }
    
    @Test
    @DisplayName("Should return empty when SKU not found")
    void testFindBySkuNotFound() {
        Optional<Product> found = productRepository.findBySku("NON-EXISTENT");
        
        assertThat(found).isEmpty();
    }
    
    @Test
    @DisplayName("Should find products by name containing")
    void testFindByNameContaining() {
        List<Product> products = productRepository.findByNameContainingIgnoreCase("pro");
        
        assertThat(products).hasSize(2);
        assertThat(products).extracting("name")
            .containsExactlyInAnyOrder("MacBook Pro", "AirPods Pro");
    }
    
    @Test
    @DisplayName("Should find products by price range")
    void testFindByPriceBetween() {
        List<Product> products = productRepository.findByPriceBetween(
            new BigDecimal("200.00"), new BigDecimal("1000.00"));
        
        assertThat(products).hasSize(2);
        assertThat(products).extracting("name")
            .containsExactlyInAnyOrder("iPhone 15", "AirPods Pro");
    }
    
    @Test
    @DisplayName("Should find products with quantity greater than")
    void testFindByQuantityGreaterThan() {
        List<Product> products = productRepository.findByQuantityGreaterThan(10);
        
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("iPhone 15");
    }
    
    @Test
    @DisplayName("Should check if product exists by SKU")
    void testExistsBySku() {
        assertThat(productRepository.existsBySku("APP-000001")).isTrue();
        assertThat(productRepository.existsBySku("NON-EXISTENT")).isFalse();
    }
    
    @Test
    @DisplayName("Should count products with low stock")
    void testCountByQuantityLessThan() {
        long count = productRepository.countByQuantityLessThan(20);
        
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should find low stock products")
    void testFindLowStockProducts() {
        List<Product> products = productRepository.findLowStockProducts(20);
        
        assertThat(products).hasSize(2);
        assertThat(products).isSortedAccordingTo(Comparator.comparing(Product::getQuantity));
    }
    
    @Test
    @DisplayName("Should find expensive products")
    void testFindExpensiveProducts() {
        List<Product> products = productRepository.findExpensiveProducts(new BigDecimal("500.00"));
        
        assertThat(products).hasSize(2);
        assertThat(products).isSortedAccordingTo((p1, p2) -> 
            p2.getPrice().compareTo(p1.getPrice()));
    }
    
    @Test
    @DisplayName("Should find products by name and price range")
    void testFindByNameContainingAndPriceBetween() {
        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndPriceBetween(
            "iPhone", new BigDecimal("500.00"), new BigDecimal("1500.00"));
        
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("iPhone 15");
    }
    
    @Test
    @DisplayName("Should decrement stock")
    void testDecrementStock() {
        Long productId = testProduct1.getId();
        Integer originalQuantity = testProduct1.getQuantity();
        
        productRepository.decrementStock(productId, 5);
        entityManager.flush();
        entityManager.clear();
        
        Product updated = productRepository.findById(productId).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(originalQuantity - 5);
    }
    
    @Test
    @DisplayName("Should increment stock")
    void testIncrementStock() {
        Long productId = testProduct1.getId();
        Integer originalQuantity = testProduct1.getQuantity();
        
        productRepository.incrementStock(productId, 10);
        entityManager.flush();
        entityManager.clear();
        
        Product updated = productRepository.findById(productId).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(originalQuantity + 10);
    }
    
    @Test
    @DisplayName("Should find recent products in stock with native query")
    void testFindRecentProductsInStock() {
        List<Product> products = productRepository.findRecentProductsInStock(
            new BigDecimal("200.00"), 2);
        
        assertThat(products).hasSize(2);
        assertThat(products).extracting("price")
            .allMatch(price -> ((BigDecimal) price).compareTo(new BigDecimal("200.00")) > 0);
    }
    
    private Product createProduct(String name, BigDecimal price, int quantity, String sku) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setSku(sku);
        product.setContactEmail("test@example.com");
        product.setDescription("Test product");
        return product;
    }
}