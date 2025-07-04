# Advanced Spring Boot Labs

## Prerequisites

- **Java 17+** (required for Spring Boot 3.5.3)
- **Spring Boot 3.5.3**
- **Gradle 8.14.2**
- **IDE** with Spring Boot support (IntelliJ IDEA, VS Code, or Eclipse)
- **Basic Spring Boot knowledge** (complete basic labs first)

## Overview

These advanced labs build upon the basic Spring Boot concepts and demonstrate enterprise-level patterns and best practices. You'll create a complete shopping application with proper layering, validation, transaction management, and exception handling.

The labs follow a systematic approach that mirrors real-world development:

1. **Entity Design** - Start with the domain model
2. **Data Access** - Spring Data JPA repository
3. **Database Initialization** - CommandLineRunner for sample data
4. **Input Validation** - Bean Validation with comprehensive testing
5. **Service Layer** - Business logic with transaction management
6. **REST Controllers** - Complete CRUD API with proper HTTP semantics
7. **Exception Handling** - Global error handling with ProblemDetail

## Project Setup

Create a new Spring Boot project called `shopping` with the following structure:

```
shopping/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── kousenit/
│   │   │           └── shopping/
│   │   │               ├── ShoppingApplication.java
│   │   │               ├── config/
│   │   │               ├── controllers/
│   │   │               ├── entities/
│   │   │               ├── repositories/
│   │   │               └── services/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── data.sql (optional)
│   └── test/
│       └── java/
│           └── com/
│               └── kousenit/
│                   └── shopping/
└── build.gradle
```

### Initial build.gradle

```gradle
plugins {
    id 'org.springframework.boot' version '3.5.3'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'java'
}

group = 'com.kousenit'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    runtimeOnly 'com.h2database:h2'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:h2'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### Initial application.yml

Create `src/main/resources/application.yml` with basic configuration that we'll expand in later labs:

```yaml
spring:
  application:
    name: shopping
  
  datasource:
    url: jdbc:h2:mem:shopping
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    database-platform: org.hibernate.dialect.H2Dialect

server:
  port: 8080
```

## Lab 1: Create the Product Entity (POJO)

**Objective**: Create a basic Product entity class that will serve as our domain model. We'll start with essential fields and build upon this foundation in later labs.

**Why POJO and not Record**: We're using JPA, which requires mutable objects with default constructors and setters. Records are immutable and better suited for DTOs or value objects.

**Using Lombok**: We'll use Lombok annotations to reduce boilerplate code while maintaining readability and focusing on the business logic.

**⚠️ IDE Setup Required**: Lombok requires an IDE plugin to work properly:
- **IntelliJ IDEA**: File → Settings → Plugins → Search "Lombok" → Install
- **VS Code**: Install "Lombok Annotations Support for VS Code" extension
- **Eclipse**: Download lombok.jar and run `java -jar lombok.jar` to install

### Step 1.1: Create the Basic Product Class

Create `src/main/java/com/kousenit/shopping/entities/Product.java`:

```java
package com.kousenit.shopping.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    // Additional fields to be enhanced with validation in Lab 4
    private String description;
    private Integer quantity;
    private String sku;
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
    
    // Business methods for stock management
    public boolean hasStock(int requestedQuantity) {
        return this.quantity != null && this.quantity >= requestedQuantity;
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
        if (this.quantity == null) {
            this.quantity = amount;
        } else {
            this.quantity += amount;
        }
    }
}
```

### Step 1.2: Create a Basic Test

Create `src/test/java/com/kousenit/shopping/entities/ProductTest.java`:

```java
package com.kousenit.shopping.entities;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {
    
    @Test
    void testProductCreation() {
        Product product = new Product("Laptop", new BigDecimal("999.99"));
        
        assertNull(product.getId()); // ID not set until persisted
        assertEquals("Laptop", product.getName());
        assertEquals(new BigDecimal("999.99"), product.getPrice());
    }
    
    @Test
    void testProductEquality() {
        Product product1 = new Product(1L, "Laptop", new BigDecimal("999.99"));
        Product product2 = new Product(1L, "Laptop", new BigDecimal("999.99"));
        Product product3 = new Product(2L, "Mouse", new BigDecimal("29.99"));
        
        assertEquals(product1, product2);
        assertNotEquals(product1, product3);
    }
    
    @Test
    void testProductToString() {
        Product product = new Product(1L, "Laptop", new BigDecimal("999.99"));
        String result = product.toString();
        
        assertTrue(result.contains("Laptop"));
        assertTrue(result.contains("999.99"));
        assertTrue(result.contains("id=1"));
    }
}
```

### Step 1.3: Run and Verify

```bash
cd shopping
./gradlew test --tests ProductTest
```

**Key Learning Points:**
- **JPA Entity Annotations**: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`
- **Why POJOs**: Mutable state required for JPA persistence
- **BigDecimal for Money**: Avoid floating-point precision issues
- **Proper equals/hashCode**: Essential for collections and JPA

## Lab 2: Create the Repository Layer

**Objective**: Implement the data access layer using Spring Data JPA.

### Step 2.1: Create the ProductRepository Interface

Create `src/main/java/com/kousenit/shopping/repositories/ProductRepository.java`:

```java
package com.kousenit.shopping.repositories;

import com.kousenit.shopping.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Derived query methods (Spring Data JPA generates implementation)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    Optional<Product> findByNameIgnoreCase(String name);
    
    List<Product> findByPriceLessThan(BigDecimal price);
    
    List<Product> findByPriceGreaterThan(BigDecimal price);
    
    // Custom query using JPQL
    @Query("SELECT p FROM Product p WHERE p.price < :maxPrice ORDER BY p.price DESC")
    List<Product> findProductsUnderPrice(@Param("maxPrice") BigDecimal maxPrice);
    
    // Native SQL query example
    @Query(value = "SELECT * FROM products WHERE price = (SELECT MAX(price) FROM products)", 
           nativeQuery = true)
    Optional<Product> findMostExpensiveProduct();
    
    // Count queries
    long countByPriceGreaterThan(BigDecimal price);
    
    // Check existence
    boolean existsByNameIgnoreCase(String name);
}
```

### Step 2.2: Create Repository Integration Tests

Create `src/test/java/com/kousenit/shopping/repositories/ProductRepositoryTest.java`:

```java
package com.kousenit.shopping.repositories;

import com.kousenit.shopping.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ProductRepository productRepository;
    
    private Product laptop;
    private Product mouse;
    private Product keyboard;
    
    @BeforeEach
    void setUp() {
        laptop = new Product("Gaming Laptop", new BigDecimal("1299.99"));
        mouse = new Product("Wireless Mouse", new BigDecimal("29.99"));
        keyboard = new Product("Mechanical Keyboard", new BigDecimal("89.99"));
        
        entityManager.persist(laptop);
        entityManager.persist(mouse);
        entityManager.persist(keyboard);
        entityManager.flush();
    }
    
    @Test
    void testFindAll() {
        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(3);
    }
    
    @Test
    void testFindByNameContainingIgnoreCase() {
        List<Product> products = productRepository.findByNameContainingIgnoreCase("mouse");
        
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Wireless Mouse");
    }
    
    @Test
    void testFindByPriceBetween() {
        List<Product> products = productRepository.findByPriceBetween(
            new BigDecimal("50.00"), 
            new BigDecimal("100.00")
        );
        
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Mechanical Keyboard");
    }
    
    @Test
    void testFindByNameIgnoreCase() {
        Optional<Product> product = productRepository.findByNameIgnoreCase("GAMING LAPTOP");
        
        assertThat(product).isPresent();
        assertThat(product.get().getPrice()).isEqualTo(new BigDecimal("1299.99"));
    }
    
    @Test
    void testCustomQueryFindProductsUnderPrice() {
        List<Product> products = productRepository.findProductsUnderPrice(new BigDecimal("100.00"));
        
        assertThat(products).hasSize(2);
        // Results should be ordered by price DESC
        assertThat(products.get(0).getPrice()).isGreaterThan(products.get(1).getPrice());
    }
    
    @Test
    void testFindMostExpensiveProduct() {
        Optional<Product> product = productRepository.findMostExpensiveProduct();
        
        assertThat(product).isPresent();
        assertThat(product.get().getName()).isEqualTo("Gaming Laptop");
    }
    
    @Test
    void testCountByPriceGreaterThan() {
        long count = productRepository.countByPriceGreaterThan(new BigDecimal("50.00"));
        
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    void testExistsByNameIgnoreCase() {
        boolean exists = productRepository.existsByNameIgnoreCase("wireless mouse");
        
        assertThat(exists).isTrue();
    }
    
    @Test
    void testExistsByNameIgnoreCaseNotFound() {
        boolean exists = productRepository.existsByNameIgnoreCase("Tablet");
        
        assertThat(exists).isFalse();
    }
    
    @Test
    void testSaveAndFindById() {
        Product tablet = new Product("iPad Pro", new BigDecimal("799.99"));
        Product saved = productRepository.save(tablet);
        
        assertThat(saved.getId()).isNotNull();
        
        Optional<Product> found = productRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("iPad Pro");
    }
    
    @Test
    void testDeleteById() {
        Long laptopId = laptop.getId();
        productRepository.deleteById(laptopId);
        
        Optional<Product> found = productRepository.findById(laptopId);
        assertThat(found).isEmpty();
    }
}
```

### Step 2.3: Run Repository Tests

```bash
./gradlew test --tests ProductRepositoryTest
```

**Key Learning Points:**
- **Spring Data JPA Magic**: Method name conventions auto-generate queries
- **@DataJpaTest**: Loads only JPA-related components for faster testing
- **TestEntityManager**: Provides test-specific persistence operations
- **Custom Queries**: JPQL vs Native SQL approaches
- **Repository Testing Patterns**: Setup, execution, and assertion patterns

## Lab 3: Database Initialization with CommandLineRunner

**Objective**: Populate the database with sample data on application startup using Spring Boot's CommandLineRunner.

### Step 3.1: Create the AppConfig Class

Create `src/main/java/com/kousenit/shopping/config/AppConfig.java`:

```java
package com.kousenit.shopping.config;

import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class AppConfig {
    
    @Bean
    @Profile("!test") // Don't run during tests
    public CommandLineRunner initializeDatabase(ProductRepository productRepository) {
        return args -> {
            // Only initialize if database is empty
            if (productRepository.count() == 0) {
                List<Product> initialProducts = List.of(
                    new Product("MacBook Pro 16\"", new BigDecimal("2499.99")),
                    new Product("iPad Air", new BigDecimal("599.99")),
                    new Product("iPhone 15 Pro", new BigDecimal("999.99")),
                    new Product("AirPods Pro", new BigDecimal("249.99")),
                    new Product("Magic Mouse", new BigDecimal("79.99")),
                    new Product("Magic Keyboard", new BigDecimal("179.99")),
                    new Product("Apple Watch Series 9", new BigDecimal("399.99")),
                    new Product("Studio Display", new BigDecimal("1599.99")),
                    new Product("Mac Studio", new BigDecimal("1999.99")),
                    new Product("HomePod mini", new BigDecimal("99.99"))
                );
                
                productRepository.saveAll(initialProducts);
                System.out.println("Initialized database with " + initialProducts.size() + " products");
                
                // Print some statistics
                long totalProducts = productRepository.count();
                System.out.println("Total products in database: " + totalProducts);
                
                productRepository.findMostExpensiveProduct()
                    .ifPresent(product -> 
                        System.out.println("Most expensive product: " + product.getName() + 
                                         " - $" + product.getPrice()));
            } else {
                System.out.println("Database already contains " + productRepository.count() + " products");
            }
        };
    }
    
    @Bean
    @Profile("demo")
    public CommandLineRunner demonstrateQueries(ProductRepository productRepository) {
        return args -> {
            System.out.println("\n=== Product Query Demonstrations ===");
            
            // Find products under $200
            System.out.println("\nProducts under $200:");
            productRepository.findProductsUnderPrice(new BigDecimal("200.00"))
                .forEach(product -> System.out.println("  " + product.getName() + " - $" + product.getPrice()));
            
            // Find products containing "Pro"
            System.out.println("\nProducts containing 'Pro':");
            productRepository.findByNameContainingIgnoreCase("Pro")
                .forEach(product -> System.out.println("  " + product.getName()));
            
            // Count expensive products (> $500)
            long expensiveCount = productRepository.countByPriceGreaterThan(new BigDecimal("500.00"));
            System.out.println("\nNumber of products over $500: " + expensiveCount);
            
            // Find products in price range
            System.out.println("\nProducts between $100 and $300:");
            productRepository.findByPriceBetween(new BigDecimal("100.00"), new BigDecimal("300.00"))
                .forEach(product -> System.out.println("  " + product.getName() + " - $" + product.getPrice()));
        };
    }
}
```

### Step 3.2: Create Configuration Tests

Create `src/test/java/com/kousenit/shopping/config/AppConfigTest.java`:

```java
package com.kousenit.shopping.config;

import com.kousenit.shopping.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // This prevents CommandLineRunner from executing
class AppConfigTest {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    void testDatabaseInitializationIsSkippedInTestProfile() {
        // With @Profile("!test") on CommandLineRunner, database should be empty
        long count = productRepository.count();
        assertThat(count).isEqualTo(0);
    }
}
```

### Step 3.3: Test Manual Database Initialization

Create `src/test/java/com/kousenit/shopping/config/DatabaseInitializationTest.java`:

```java
package com.kousenit.shopping.config;

import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("integration") // Different profile to test initialization
class DatabaseInitializationTest {
    
    @MockitoBean
    private ProductRepository productRepository;
    
    @Autowired
    private AppConfig appConfig;
    
    @Test
    void testCommandLineRunnerInitializesEmptyDatabase() throws Exception {
        // Given: Empty database
        when(productRepository.count()).thenReturn(0L);
        
        // When: CommandLineRunner executes
        appConfig.initializeDatabase(productRepository).run();
        
        // Then: Products are saved
        verify(productRepository).count();
        verify(productRepository).saveAll(anyList());
        verify(productRepository).findMostExpensiveProduct();
    }
    
    @Test
    void testCommandLineRunnerSkipsNonEmptyDatabase() throws Exception {
        // Given: Database with existing data
        when(productRepository.count()).thenReturn(5L);
        
        // When: CommandLineRunner executes
        appConfig.initializeDatabase(productRepository).run();
        
        // Then: No products are saved
        verify(productRepository).count();
        verify(productRepository, never()).saveAll(anyList());
    }
}
```

### Step 3.4: Create Integration Test

Create `src/test/java/com/kousenit/shopping/ShoppingApplicationTest.java`:

```java
package com.kousenit.shopping;

import com.kousenit.shopping.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration")
class ShoppingApplicationTest {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    void contextLoads() {
        // Verify the application context loads successfully
        assertThat(productRepository).isNotNull();
    }
    
    @Test
    void testDatabaseIsInitialized() {
        // When running with default profile, database should be initialized
        long count = productRepository.count();
        assertThat(count).isGreaterThan(0);
        
        // Verify some expected products exist
        boolean hasAppleProducts = productRepository.findByNameContainingIgnoreCase("Apple").size() > 0;
        boolean hasMacProducts = productRepository.findByNameContainingIgnoreCase("Mac").size() > 0;
        
        assertThat(hasAppleProducts || hasMacProducts).isTrue();
    }
}
```

### Step 3.5: Run the Application

```bash
# Run with default profile (initializes database)
./gradlew bootRun

# Run with demo profile (shows query demonstrations)
./gradlew bootRun --args='--spring.profiles.active=demo'

# Check H2 Console
# URL: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:shopping
# Username: sa
# Password: (empty)
```

### Step 3.6: Run Tests

```bash
# Run all tests
./gradlew test

# Run specific test classes
./gradlew test --tests AppConfigTest
./gradlew test --tests DatabaseInitializationTest
./gradlew test --tests ShoppingApplicationTest
```

### Step 3.4: Update application.yml with Test Profile Configuration

Update `src/main/resources/application.yml` to add test profile configuration using YAML's `---` separator:

```yaml
spring:
  application:
    name: shopping
  
  datasource:
    url: jdbc:h2:mem:shopping
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    database-platform: org.hibernate.dialect.H2Dialect

server:
  port: 8080

---
# Test profile configuration
spring:
  config:
    activate:
      on-profile: test
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    show-sql: false
```

**Why YAML Profile Separation:**
- **Single File**: All profiles in one `application.yml` file
- **`---` Separator**: Clearly separates different profile configurations
- **Test Isolation**: Separate database and reduced logging for tests
- **Maintainability**: Easier to manage than multiple property files

**Key Learning Points:**
- **CommandLineRunner**: Execute code after Spring Boot application startup
- **@Profile Annotations**: Control bean creation based on active profiles
- **Conditional Initialization**: Check database state before populating
- **Separation of Concerns**: Configuration logic separate from business logic
- **Test Profiles**: Prevent side effects during testing
- **YAML Profiles**: Use `---` separator for profile-specific configuration
- **Integration Testing**: Test actual application behavior with real dependencies

## Lab 4: Add Bean Validation Annotations

**Objective**: Add comprehensive validation to the Product entity and test validation behavior at multiple layers.

### Step 4.1: Enhanced Product Entity with Validation and Indexes

Update `src/main/java/com/kousenit/shopping/entities/Product.java` to match our final implementation:

```java
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
    
    // Business methods for stock management
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
```

### Step 4.2: Bean Validation Unit Tests

Create `src/test/java/com/kousenit/shopping/entities/ProductValidationTest.java`:

```java
package com.kousenit.shopping.entities;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProductValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidProduct() {
        Product product = new Product();
        product.setName("MacBook Pro");
        product.setPrice(new BigDecimal("2499.99"));
        product.setDescription("High-performance laptop");
        product.setQuantity(5);
        product.setSku("MAC-123456");
        product.setContactEmail("sales@apple.com");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).isEmpty();
    }
    
    @Test
    void testBlankNameValidation() {
        Product product = new Product("", new BigDecimal("100.00"));
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        ConstraintViolation<Product> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Product name cannot be blank");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }
    
    @Test
    void testNameTooShort() {
        Product product = new Product("A", new BigDecimal("100.00"));
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Product name must be between 2 and 100 characters");
    }
    
    @Test
    void testNameTooLong() {
        String longName = "A".repeat(101);
        Product product = new Product(longName, new BigDecimal("100.00"));
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Product name must be between 2 and 100 characters");
    }
    
    @Test
    void testNullPriceValidation() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(null);
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Price cannot be null");
    }
    
    @Test
    void testPriceTooLow() {
        Product product = new Product("Test Product", new BigDecimal("0.00"));
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Price must be at least $0.01");
    }
    
    @Test
    void testPriceTooHigh() {
        Product product = new Product("Test Product", new BigDecimal("100000.00"));
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Price cannot exceed $99,999.99");
    }
    
    @Test
    void testInvalidPricePrecision() {
        Product product = new Product("Test Product", new BigDecimal("100.123"));
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Price must be a valid monetary amount");
    }
    
    @Test
    void testNegativeQuantity() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setQuantity(-1);
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Quantity cannot be negative");
    }
    
    @Test
    void testQuantityTooHigh() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setQuantity(10001);
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Quantity cannot exceed 10,000");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "AB123", "AB-12", "AB-1234567", "ab-123456"})
    void testInvalidSkuFormats(String invalidSku) {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setSku(invalidSku);
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("SKU must follow format: XX-123456 (2-3 letters, dash, 3-6 digits)");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"AB-123", "ABC-123456", "XY-999999"})
    void testValidSkuFormats(String validSku) {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setSku(validSku);
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).isEmpty();
    }
    
    @Test
    void testInvalidEmail() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setContactEmail("invalid-email");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Contact email must be valid");
    }
    
    @Test
    void testDescriptionTooLong() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.00"));
        product.setDescription("A".repeat(501));
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Description cannot exceed 500 characters");
    }
    
    @Test
    void testMultipleValidationErrors() {
        Product product = new Product();
        product.setName(""); // Blank name
        product.setPrice(new BigDecimal("0.00")); // Too low price
        product.setQuantity(-5); // Negative quantity
        product.setSku("INVALID"); // Invalid SKU format
        product.setContactEmail("not-an-email"); // Invalid email
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertThat(violations).hasSize(5);
        
        // Verify we have violations for all expected fields
        Set<String> violatedProperties = violations.stream()
            .map(violation -> violation.getPropertyPath().toString())
            .collect(java.util.stream.Collectors.toSet());
        
        assertThat(violatedProperties).containsExactlyInAnyOrder(
            "name", "price", "quantity", "sku", "contactEmail"
        );
    }
}
```

### Step 4.3: Repository Validation Integration Tests

Create `src/test/java/com/kousenit/shopping/repositories/ProductRepositoryValidationTest.java`:

```java
package com.kousenit.shopping.repositories;

import com.kousenit.shopping.entities.Product;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ProductRepositoryValidationTest {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    void testSaveValidProduct() {
        Product product = new Product();
        product.setName("Valid Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setDescription("A valid product description");
        product.setQuantity(10);
        product.setSku("VP-123456");
        product.setContactEmail("test@example.com");
        
        Product saved = productRepository.save(product);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Valid Product");
    }
    
    @Test
    void testSaveProductWithNullName() {
        Product product = new Product();
        product.setName(null);
        product.setPrice(new BigDecimal("99.99"));
        
        // JPA will throw constraint violation for @NotBlank
        assertThatThrownBy(() -> {
            productRepository.save(product);
            productRepository.flush(); // Force immediate validation
        }).isInstanceOf(ConstraintViolationException.class);
    }
    
    @Test
    void testSaveProductWithDuplicateSku() {
        // Save first product
        Product product1 = new Product();
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("99.99"));
        product1.setSku("UNIQUE-123");
        productRepository.save(product1);
        
        // Try to save second product with same SKU
        Product product2 = new Product();
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("199.99"));
        product2.setSku("UNIQUE-123"); // Duplicate SKU
        
        assertThatThrownBy(() -> {
            productRepository.save(product2);
            productRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
    
    @Test
    void testUpdateProductWithValidData() {
        // Save initial product
        Product product = new Product();
        product.setName("Original Name");
        product.setPrice(new BigDecimal("99.99"));
        product.setSku("ORG-123456");
        Product saved = productRepository.save(product);
        
        // Update with valid data
        saved.setName("Updated Name");
        saved.setPrice(new BigDecimal("149.99"));
        saved.setDescription("Updated description");
        
        Product updated = productRepository.save(saved);
        
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getPrice()).isEqualTo(new BigDecimal("149.99"));
        assertThat(updated.getDescription()).isEqualTo("Updated description");
    }
    
    @Test
    void testFindProductsWithValidationConstraints() {
        // Save products with different price ranges to test our custom queries
        Product cheapProduct = new Product();
        cheapProduct.setName("Cheap Product");
        cheapProduct.setPrice(new BigDecimal("9.99"));
        cheapProduct.setSku("CP-123456");
        
        Product expensiveProduct = new Product();
        expensiveProduct.setName("Expensive Product");
        expensiveProduct.setPrice(new BigDecimal("999.99"));
        expensiveProduct.setSku("EP-123456");
        
        productRepository.save(cheapProduct);
        productRepository.save(expensiveProduct);
        
        // Test our custom query methods work with validated entities
        var cheapProducts = productRepository.findByPriceLessThan(new BigDecimal("50.00"));
        var expensiveProducts = productRepository.findByPriceGreaterThan(new BigDecimal("500.00"));
        
        assertThat(cheapProducts).hasSize(1);
        assertThat(cheapProducts.get(0).getName()).isEqualTo("Cheap Product");
        
        assertThat(expensiveProducts).hasSize(1);
        assertThat(expensiveProducts.get(0).getName()).isEqualTo("Expensive Product");
    }
}
```

### Step 4.4: Update AppConfig with Validated Products

Update the `AppConfig.java` to include properly validated sample data:

```java
// In the initializeDatabase method, replace the product creation with:
List<Product> initialProducts = List.of(
    createProduct("MacBook Pro 16\"", "2499.99", "High-performance laptop for professionals", 5, "MAC-001001", "sales@apple.com"),
    createProduct("iPad Air", "599.99", "Lightweight tablet with M1 chip", 10, "IPD-002001", "sales@apple.com"),
    createProduct("iPhone 15 Pro", "999.99", "Latest smartphone with titanium design", 15, "IPH-003001", "sales@apple.com"),
    createProduct("AirPods Pro", "249.99", "Wireless earbuds with active noise cancellation", 25, "APD-004001", "sales@apple.com"),
    createProduct("Magic Mouse", "79.99", "Wireless multi-touch mouse", 20, "MOU-005001", "sales@apple.com"),
    createProduct("Magic Keyboard", "179.99", "Wireless keyboard with numeric keypad", 12, "KEY-006001", "sales@apple.com"),
    createProduct("Apple Watch Series 9", "399.99", "Advanced health and fitness tracking", 18, "WAT-007001", "sales@apple.com"),
    createProduct("Studio Display", "1599.99", "27-inch 5K Retina display", 3, "DIS-008001", "sales@apple.com"),
    createProduct("Mac Studio", "1999.99", "Desktop computer with M2 Max chip", 4, "MAC-009001", "sales@apple.com"),
    createProduct("HomePod mini", "99.99", "Smart speaker with Siri", 30, "HOM-010001", "sales@apple.com")
);

// Add this helper method to AppConfig:
private Product createProduct(String name, String price, String description, 
                             int quantity, String sku, String email) {
    Product product = new Product();
    product.setName(name);
    product.setPrice(new BigDecimal(price));
    product.setDescription(description);
    product.setQuantity(quantity);
    product.setSku(sku);
    product.setContactEmail(email);
    return product;
}
```

### Step 4.5: Run Validation Tests

```bash
# Run validation-specific tests
./gradlew test --tests ProductValidationTest
./gradlew test --tests ProductRepositoryValidationTest

# Run all tests to ensure nothing is broken
./gradlew test

# Start the application to see validation in action
./gradlew bootRun
```

### Step 4.6: Test Validation in H2 Console

1. Start the application: `./gradlew bootRun`
2. Go to H2 Console: http://localhost:8080/h2-console
3. Try invalid SQL inserts to see constraint violations:

```sql
-- This should fail due to validation constraints
INSERT INTO products (name, price, quantity, sku, contact_email) 
VALUES ('', 0.00, -1, 'INVALID', 'not-an-email');

-- This should succeed
INSERT INTO products (name, price, description, quantity, sku, contact_email) 
VALUES ('Test Product', 99.99, 'A test product', 5, 'TST-123456', 'test@example.com');
```

**Key Learning Points:**
- **Bean Validation Annotations**: @NotBlank, @Size, @DecimalMin, @Pattern, @Email, etc.
- **Validation Layers**: Entity-level, repository-level, and application-level validation
- **Constraint Violation Testing**: Using Validator for unit tests
- **Integration Testing**: Testing validation with actual persistence
- **Custom Validation Messages**: User-friendly error messages
- **Parameterized Tests**: Testing multiple invalid inputs efficiently
- **Database Constraints**: How validation translates to database constraints

## Lab 5: Add Service Layer with @Transactional

**Objective**: Create a service layer that encapsulates business logic and demonstrates transaction management with @Transactional.

### Step 5.1: Create Custom Exceptions

First, create domain-specific exceptions for business rules:

Create `src/main/java/com/kousenit/shopping/exceptions/ProductNotFoundException.java`:

```java
package com.kousenit.shopping.exceptions;

public class ProductNotFoundException extends RuntimeException {
    private final Long productId;
    
    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
        this.productId = productId;
    }
    
    public ProductNotFoundException(String message) {
        super(message);
    }
    
    public Long getProductId() {
        return productId;
    }
}
```

Create `src/main/java/com/kousenit/shopping/exceptions/ProductValidationException.java`:

```java
package com.kousenit.shopping.exceptions;

public class ProductValidationException extends RuntimeException {
    private final String field;
    private final Object value;
    
    public ProductValidationException(String message) {
        super(message);
        this.field = null;
        this.value = null;
    }
    
    public ProductValidationException(String field, Object value, String message) {
        super(String.format("Invalid %s: %s. %s", field, value, message));
        this.field = field;
        this.value = value;
    }
    
    public String getField() {
        return field;
    }
    
    public Object getValue() {
        return value;
    }
}
```

Create `src/main/java/com/kousenit/shopping/exceptions/InsufficientStockException.java`:

```java
package com.kousenit.shopping.exceptions;

public class InsufficientStockException extends RuntimeException {
    private final Long productId;
    private final int requestedQuantity;
    private final int availableQuantity;
    
    public InsufficientStockException(Long productId, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient stock for product %d. Requested: %d, Available: %d", 
              productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public int getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
```

### Step 5.2: Create the ProductService Interface

Create `src/main/java/com/kousenit/shopping/services/ProductService.java`:

```java
package com.kousenit.shopping.services;

import com.kousenit.shopping.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    
    // Basic CRUD operations
    Product createProduct(Product product);
    Optional<Product> getProductById(Long id);
    Product getProductByIdOrThrow(Long id);
    List<Product> getAllProducts();
    Page<Product> getAllProducts(Pageable pageable);
    Product updateProduct(Long id, Product productUpdates);
    void deleteProduct(Long id);
    
    // Business operations
    List<Product> searchProductsByName(String name);
    List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> getProductsUnderPrice(BigDecimal maxPrice);
    Optional<Product> getMostExpensiveProduct();
    
    // Stock management
    Product updateStock(Long productId, int quantity);
    Product reserveStock(Long productId, int quantity);
    Product releaseStock(Long productId, int quantity);
    
    // Batch operations (demonstrating transactions)
    List<Product> createProducts(List<Product> products);
    void updatePrices(List<Long> productIds, BigDecimal priceMultiplier);
    void deleteProducts(List<Long> productIds);
    
    // Business logic
    boolean isProductAvailable(Long productId);
    boolean isProductInStock(Long productId, int requiredQuantity);
    long countProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Data initialization
    void initializeDatabase();
}
```

### Step 5.3: Implement the ProductService

Create `src/main/java/com/kousenit/shopping/services/ProductServiceImpl.java`:

```java
package com.kousenit.shopping.services;

import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.exceptions.InsufficientStockException;
import com.kousenit.shopping.exceptions.ProductNotFoundException;
import com.kousenit.shopping.exceptions.ProductValidationException;
import com.kousenit.shopping.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true) // Default for all methods
public class ProductServiceImpl implements ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Override
    @Transactional // Write operation - overrides readOnly=true
    public Product createProduct(Product product) {
        logger.info("Creating new product: {}", product.getName());
        
        validateProduct(product);
        
        // Check for duplicate SKU
        if (product.getSku() != null && productRepository.existsByNameIgnoreCase(product.getSku())) {
            throw new ProductValidationException("sku", product.getSku(), "SKU already exists");
        }
        
        Product saved = productRepository.save(product);
        logger.info("Created product with ID: {}", saved.getId());
        return saved;
    }
    
    @Override
    public Optional<Product> getProductById(Long id) {
        logger.debug("Fetching product with ID: {}", id);
        return productRepository.findById(id);
    }
    
    @Override
    public Product getProductByIdOrThrow(Long id) {
        return getProductById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }
    
    @Override
    public List<Product> getAllProducts() {
        logger.debug("Fetching all products");
        return productRepository.findAll();
    }
    
    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        logger.debug("Fetching products page: {}", pageable);
        return productRepository.findAll(pageable);
    }
    
    @Override
    @Transactional
    public Product updateProduct(Long id, Product productUpdates) {
        logger.info("Updating product with ID: {}", id);
        
        Product existingProduct = getProductByIdOrThrow(id);
        
        // Update fields if provided
        if (productUpdates.getName() != null) {
            existingProduct.setName(productUpdates.getName());
        }
        if (productUpdates.getPrice() != null) {
            existingProduct.setPrice(productUpdates.getPrice());
        }
        if (productUpdates.getDescription() != null) {
            existingProduct.setDescription(productUpdates.getDescription());
        }
        if (productUpdates.getQuantity() != null) {
            existingProduct.setQuantity(productUpdates.getQuantity());
        }
        if (productUpdates.getSku() != null) {
            existingProduct.setSku(productUpdates.getSku());
        }
        if (productUpdates.getContactEmail() != null) {
            existingProduct.setContactEmail(productUpdates.getContactEmail());
        }
        
        validateProduct(existingProduct);
        Product updated = productRepository.save(existingProduct);
        logger.info("Updated product: {}", updated.getName());
        return updated;
    }
    
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);
        
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        
        productRepository.deleteById(id);
        logger.info("Deleted product with ID: {}", id);
    }
    
    @Override
    public List<Product> searchProductsByName(String name) {
        logger.debug("Searching products by name: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }
    
    @Override
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        logger.debug("Fetching products in price range: {} - {}", minPrice, maxPrice);
        
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new ProductValidationException("price range", 
                String.format("%s-%s", minPrice, maxPrice), 
                "Minimum price cannot be greater than maximum price");
        }
        
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    @Override
    public List<Product> getProductsUnderPrice(BigDecimal maxPrice) {
        logger.debug("Fetching products under price: {}", maxPrice);
        return productRepository.findProductsUnderPrice(maxPrice);
    }
    
    @Override
    public Optional<Product> getMostExpensiveProduct() {
        logger.debug("Fetching most expensive product");
        return productRepository.findMostExpensiveProduct();
    }
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Product updateStock(Long productId, int quantity) {
        logger.info("Updating stock for product {}: {}", productId, quantity);
        
        Product product = getProductByIdOrThrow(productId);
        
        if (quantity < 0) {
            throw new ProductValidationException("quantity", quantity, "Quantity cannot be negative");
        }
        
        product.setQuantity(quantity);
        Product updated = productRepository.save(product);
        logger.info("Updated stock for product {}: {}", productId, quantity);
        return updated;
    }
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Product reserveStock(Long productId, int quantity) {
        logger.info("Reserving stock for product {}: {}", productId, quantity);
        
        Product product = getProductByIdOrThrow(productId);
        
        if (product.getQuantity() < quantity) {
            throw new InsufficientStockException(productId, quantity, product.getQuantity());
        }
        
        product.setQuantity(product.getQuantity() - quantity);
        Product updated = productRepository.save(product);
        logger.info("Reserved {} units for product {}", quantity, productId);
        return updated;
    }
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Product releaseStock(Long productId, int quantity) {
        logger.info("Releasing stock for product {}: {}", productId, quantity);
        
        Product product = getProductByIdOrThrow(productId);
        product.setQuantity(product.getQuantity() + quantity);
        Product updated = productRepository.save(product);
        logger.info("Released {} units for product {}", quantity, productId);
        return updated;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<Product> createProducts(List<Product> products) {
        logger.info("Creating {} products in batch", products.size());
        
        // Validate all products first
        products.forEach(this::validateProduct);
        
        List<Product> savedProducts = productRepository.saveAll(products);
        logger.info("Successfully created {} products", savedProducts.size());
        return savedProducts;
    }
    
    @Override
    @Transactional
    public void updatePrices(List<Long> productIds, BigDecimal priceMultiplier) {
        logger.info("Updating prices for {} products with multiplier: {}", productIds.size(), priceMultiplier);
        
        if (priceMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductValidationException("priceMultiplier", priceMultiplier, 
                "Price multiplier must be positive");
        }
        
        for (Long productId : productIds) {
            Product product = getProductByIdOrThrow(productId);
            BigDecimal newPrice = product.getPrice().multiply(priceMultiplier);
            product.setPrice(newPrice);
            productRepository.save(product);
            logger.debug("Updated price for product {}: {}", productId, newPrice);
        }
        
        logger.info("Completed price update for {} products", productIds.size());
    }
    
    @Override
    @Transactional
    public void deleteProducts(List<Long> productIds) {
        logger.info("Deleting {} products", productIds.size());
        
        // Verify all products exist first
        for (Long productId : productIds) {
            if (!productRepository.existsById(productId)) {
                throw new ProductNotFoundException(productId);
            }
        }
        
        // Delete all products
        productRepository.deleteAllById(productIds);
        logger.info("Successfully deleted {} products", productIds.size());
    }
    
    @Override
    public boolean isProductAvailable(Long productId) {
        return productRepository.existsById(productId);
    }
    
    @Override
    public boolean isProductInStock(Long productId, int requiredQuantity) {
        return getProductById(productId)
            .map(product -> product.getQuantity() >= requiredQuantity)
            .orElse(false);
    }
    
    @Override
    public long countProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice).size();
    }
    
    @Override
    @Transactional
    public void initializeDatabase() {
        logger.info("Initializing database with sample products");
        
        if (productRepository.count() > 0) {
            logger.info("Database already contains {} products, skipping initialization", 
                productRepository.count());
            return;
        }
        
        List<Product> sampleProducts = List.of(
            createSampleProduct("MacBook Pro 16\"", "2499.99", "High-performance laptop for professionals", 5, "MAC-001001", "sales@apple.com"),
            createSampleProduct("iPad Air", "599.99", "Lightweight tablet with M1 chip", 10, "IPD-002001", "sales@apple.com"),
            createSampleProduct("iPhone 15 Pro", "999.99", "Latest smartphone with titanium design", 15, "IPH-003001", "sales@apple.com"),
            createSampleProduct("AirPods Pro", "249.99", "Wireless earbuds with active noise cancellation", 25, "APD-004001", "sales@apple.com"),
            createSampleProduct("Magic Mouse", "79.99", "Wireless multi-touch mouse", 20, "MOU-005001", "sales@apple.com"),
            createSampleProduct("Magic Keyboard", "179.99", "Wireless keyboard with numeric keypad", 12, "KEY-006001", "sales@apple.com"),
            createSampleProduct("Apple Watch Series 9", "399.99", "Advanced health and fitness tracking", 18, "WAT-007001", "sales@apple.com"),
            createSampleProduct("Studio Display", "1599.99", "27-inch 5K Retina display", 3, "DIS-008001", "sales@apple.com"),
            createSampleProduct("Mac Studio", "1999.99", "Desktop computer with M2 Max chip", 4, "MAC-009001", "sales@apple.com"),
            createSampleProduct("HomePod mini", "99.99", "Smart speaker with Siri", 30, "HOM-010001", "sales@apple.com")
        );
        
        createProducts(sampleProducts);
        logger.info("Database initialization completed with {} products", sampleProducts.size());
    }
    
    private void validateProduct(Product product) {
        if (product == null) {
            throw new ProductValidationException("Product cannot be null");
        }
        
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new ProductValidationException("name", product.getName(), "Product name is required");
        }
        
        if (product.getPrice() == null) {
            throw new ProductValidationException("price", null, "Product price is required");
        }
        
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductValidationException("price", product.getPrice(), "Price must be positive");
        }
    }
    
    private Product createSampleProduct(String name, String price, String description, 
                                       int quantity, String sku, String email) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setDescription(description);
        product.setQuantity(quantity);
        product.setSku(sku);
        product.setContactEmail(email);
        return product;
    }
}
```

### Step 5.4: Update AppConfig to Use Service

Update `src/main/java/com/kousenit/shopping/config/AppConfig.java`:

```java
package com.kousenit.shopping.config;

import com.kousenit.shopping.services.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AppConfig {
    
    @Bean
    @Profile("!test") // Don't run during tests
    public CommandLineRunner initializeDatabase(ProductService productService) {
        return args -> productService.initializeDatabase();
    }
    
    @Bean
    @Profile("demo")
    public CommandLineRunner demonstrateServiceOperations(ProductService productService) {
        return args -> {
            System.out.println("\n=== Service Layer Demonstrations ===");
            
            // Search operations
            System.out.println("\nProducts containing 'Pro':");
            productService.searchProductsByName("Pro")
                .forEach(product -> System.out.println("  " + product.getName() + " - $" + product.getPrice()));
            
            // Price range operations
            System.out.println("\nExpensive products (>$1000):");
            productService.getProductsByPriceRange(new java.math.BigDecimal("1000.00"), new java.math.BigDecimal("10000.00"))
                .forEach(product -> System.out.println("  " + product.getName() + " - $" + product.getPrice()));
            
            // Most expensive product
            productService.getMostExpensiveProduct()
                .ifPresent(product -> System.out.println("\nMost expensive: " + product.getName() + " - $" + product.getPrice()));
            
            // Stock operations
            System.out.println("\nStock availability check:");
            System.out.println("  iPhone in stock (5 units): " + productService.isProductInStock(3L, 5));
            System.out.println("  iPhone in stock (50 units): " + productService.isProductInStock(3L, 50));
        };
    }
}
```

### Step 5.5: Service Unit Tests

Create `src/test/java/com/kousenit/shopping/services/ProductServiceTest.java`:

```java
package com.kousenit.shopping.services;

import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.exceptions.InsufficientStockException;
import com.kousenit.shopping.exceptions.ProductNotFoundException;
import com.kousenit.shopping.exceptions.ProductValidationException;
import com.kousenit.shopping.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductServiceImpl productService;
    
    private Product sampleProduct;
    
    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("Test Product");
        sampleProduct.setPrice(new BigDecimal("99.99"));
        sampleProduct.setQuantity(10);
        sampleProduct.setSku("TST-123456");
        sampleProduct.setContactEmail("test@example.com");
    }
    
    @Test
    void testCreateProductSuccess() {
        // Given
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setPrice(new BigDecimal("149.99"));
        
        when(productRepository.existsByNameIgnoreCase(any())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        
        // When
        Product result = productService.createProduct(newProduct);
        
        // Then
        assertThat(result).isEqualTo(sampleProduct);
        verify(productRepository).save(newProduct);
    }
    
    @Test
    void testCreateProductWithNullName() {
        // Given
        Product invalidProduct = new Product();
        invalidProduct.setPrice(new BigDecimal("99.99"));
        // name is null
        
        // When/Then
        assertThatThrownBy(() -> productService.createProduct(invalidProduct))
            .isInstanceOf(ProductValidationException.class)
            .hasMessageContaining("Product name is required");
        
        verify(productRepository, never()).save(any());
    }
    
    @Test
    void testGetProductByIdSuccess() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        
        // When
        Optional<Product> result = productService.getProductById(1L);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(sampleProduct);
    }
    
    @Test
    void testGetProductByIdNotFound() {
        // Given
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // When
        Optional<Product> result = productService.getProductById(999L);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void testGetProductByIdOrThrowNotFound() {
        // Given
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> productService.getProductByIdOrThrow(999L))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product not found with id: 999");
    }
    
    @Test
    void testUpdateProductSuccess() {
        // Given
        Product updates = new Product();
        updates.setName("Updated Name");
        updates.setPrice(new BigDecimal("199.99"));
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        
        // When
        Product result = productService.updateProduct(1L, updates);
        
        // Then
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("199.99"));
        verify(productRepository).save(sampleProduct);
    }
    
    @Test
    void testDeleteProductSuccess() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        
        // When
        productService.deleteProduct(1L);
        
        // Then
        verify(productRepository).deleteById(1L);
    }
    
    @Test
    void testDeleteProductNotFound() {
        // Given
        when(productRepository.existsById(anyLong())).thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> productService.deleteProduct(999L))
            .isInstanceOf(ProductNotFoundException.class);
        
        verify(productRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testReserveStockSuccess() {
        // Given
        sampleProduct.setQuantity(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        
        // When
        Product result = productService.reserveStock(1L, 5);
        
        // Then
        assertThat(result.getQuantity()).isEqualTo(5);
        verify(productRepository).save(sampleProduct);
    }
    
    @Test
    void testReserveStockInsufficientStock() {
        // Given
        sampleProduct.setQuantity(3);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        
        // When/Then
        assertThatThrownBy(() -> productService.reserveStock(1L, 5))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("Insufficient stock for product 1");
        
        verify(productRepository, never()).save(any());
    }
    
    @Test
    void testReleaseStock() {
        // Given
        sampleProduct.setQuantity(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        
        // When
        Product result = productService.releaseStock(1L, 3);
        
        // Then
        assertThat(result.getQuantity()).isEqualTo(8);
        verify(productRepository).save(sampleProduct);
    }
    
    @Test
    void testCreateProductsBatch() {
        // Given
        List<Product> products = List.of(
            new Product("Product 1", new BigDecimal("99.99")),
            new Product("Product 2", new BigDecimal("149.99"))
        );
        
        when(productRepository.saveAll(any())).thenReturn(products);
        
        // When
        List<Product> result = productService.createProducts(products);
        
        // Then
        assertThat(result).hasSize(2);
        verify(productRepository).saveAll(products);
    }
    
    @Test
    void testUpdatePrices() {
        // Given
        List<Long> productIds = List.of(1L, 2L);
        BigDecimal multiplier = new BigDecimal("1.1");
        
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        
        // When
        productService.updatePrices(productIds, multiplier);
        
        // Then
        verify(productRepository, times(2)).findById(anyLong());
        verify(productRepository, times(2)).save(any(Product.class));
    }
    
    @Test
    void testIsProductInStock() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        
        // When/Then
        assertThat(productService.isProductInStock(1L, 5)).isTrue();
        assertThat(productService.isProductInStock(1L, 15)).isFalse();
    }
    
    @Test
    void testSearchProductsByName() {
        // Given
        List<Product> searchResults = List.of(sampleProduct);
        when(productRepository.findByNameContainingIgnoreCase("Test")).thenReturn(searchResults);
        
        // When
        List<Product> result = productService.searchProductsByName("Test");
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(sampleProduct);
    }
}
```

### Step 5.6: Service Integration Tests

Create `src/test/java/com/kousenit/shopping/services/ProductServiceIntegrationTest.java`:

```java
package com.kousenit.shopping.services;

import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.exceptions.InsufficientStockException;
import com.kousenit.shopping.exceptions.ProductNotFoundException;
import com.kousenit.shopping.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceIntegrationTest {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductRepository productRepository;
    
    private Product testProduct;
    
    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setName("Integration Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setQuantity(10);
        testProduct.setSku("INT-123456");
        testProduct.setContactEmail("test@integration.com");
    }
    
    @Test
    void testCompleteProductLifecycle() {
        // Create product
        Product created = productService.createProduct(testProduct);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Integration Test Product");
        
        // Read product
        Product found = productService.getProductByIdOrThrow(created.getId());
        assertThat(found.getName()).isEqualTo("Integration Test Product");
        
        // Update product
        Product updates = new Product();
        updates.setPrice(new BigDecimal("149.99"));
        updates.setDescription("Updated description");
        
        Product updated = productService.updateProduct(created.getId(), updates);
        assertThat(updated.getPrice()).isEqualTo(new BigDecimal("149.99"));
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        
        // Delete product
        productService.deleteProduct(created.getId());
        assertThatThrownBy(() -> productService.getProductByIdOrThrow(created.getId()))
            .isInstanceOf(ProductNotFoundException.class);
    }
    
    @Test
    void testStockManagement() {
        // Create product with initial stock
        Product created = productService.createProduct(testProduct);
        assertThat(created.getQuantity()).isEqualTo(10);
        
        // Reserve stock
        Product reserved = productService.reserveStock(created.getId(), 3);
        assertThat(reserved.getQuantity()).isEqualTo(7);
        
        // Try to reserve more than available
        assertThatThrownBy(() -> productService.reserveStock(created.getId(), 10))
            .isInstanceOf(InsufficientStockException.class);
        
        // Release stock
        Product released = productService.releaseStock(created.getId(), 2);
        assertThat(released.getQuantity()).isEqualTo(9);
        
        // Update stock directly
        Product updatedStock = productService.updateStock(created.getId(), 20);
        assertThat(updatedStock.getQuantity()).isEqualTo(20);
    }
    
    @Test
    void testBatchOperations() {
        // Create multiple products
        List<Product> products = List.of(
            createTestProduct("Batch Product 1", "99.99", "BP1-123456"),
            createTestProduct("Batch Product 2", "149.99", "BP2-123456"),
            createTestProduct("Batch Product 3", "199.99", "BP3-123456")
        );
        
        List<Product> created = productService.createProducts(products);
        assertThat(created).hasSize(3);
        
        // Update prices
        List<Long> productIds = created.stream().map(Product::getId).toList();
        productService.updatePrices(productIds, new BigDecimal("1.1"));
        
        // Verify price updates
        Product updated1 = productService.getProductByIdOrThrow(productIds.get(0));
        assertThat(updated1.getPrice()).isEqualTo(new BigDecimal("109.989"));
        
        // Delete all products
        productService.deleteProducts(productIds);
        
        // Verify deletion
        for (Long id : productIds) {
            assertThatThrownBy(() -> productService.getProductByIdOrThrow(id))
                .isInstanceOf(ProductNotFoundException.class);
        }
    }
    
    @Test
    void testSearchOperations() {
        // Create test products
        productService.createProduct(createTestProduct("Apple iPhone", "999.99", "APL-001"));
        productService.createProduct(createTestProduct("Samsung Galaxy", "799.99", "SAM-001"));
        productService.createProduct(createTestProduct("Apple iPad", "599.99", "APL-002"));
        
        // Search by name
        List<Product> appleProducts = productService.searchProductsByName("Apple");
        assertThat(appleProducts).hasSize(2);
        
        // Search by price range
        List<Product> midRangeProducts = productService.getProductsByPriceRange(
            new BigDecimal("600.00"), new BigDecimal("900.00"));
        assertThat(midRangeProducts).hasSize(2);
        
        // Get products under price
        List<Product> cheapProducts = productService.getProductsUnderPrice(new BigDecimal("800.00"));
        assertThat(cheapProducts).hasSize(2);
        
        // Get most expensive
        productService.getMostExpensiveProduct()
            .ifPresent(product -> assertThat(product.getName()).contains("iPhone"));
    }
    
    private Product createTestProduct(String name, String price, String sku) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setQuantity(5);
        product.setSku(sku);
        product.setContactEmail("test@example.com");
        return product;
    }
}
```

### Step 5.7: Run Service Tests

```bash
# Run service unit tests
./gradlew test --tests ProductServiceTest

# Run service integration tests
./gradlew test --tests ProductServiceIntegrationTest

# Run all tests
./gradlew test

# Run with service demonstrations
./gradlew bootRun --args='--spring.profiles.active=demo'
```

**Key Learning Points:**
- **@Transactional**: Method-level transaction management with different propagation and isolation levels
- **Service Layer Pattern**: Business logic encapsulation and separation from controllers
- **Exception Handling**: Domain-specific exceptions for better error communication
- **Transaction Isolation**: REPEATABLE_READ for stock operations to prevent race conditions
- **Batch Operations**: Demonstrating transaction boundaries with multiple operations
- **Dependency Injection**: Constructor-based injection of repositories
- **Logging**: Structured logging for monitoring and debugging
- **Validation**: Business rule validation separate from bean validation

## Lab 6: Create REST Controller with Full CRUD Operations

**Objective**: Build a complete REST API with proper HTTP semantics, validation integration, and comprehensive testing.

### Step 6.1: Create DTOs for Request/Response

First, create Data Transfer Objects to separate API contracts from internal entities:

Create `src/main/java/com/kousenit/shopping/dto/ProductRequest.java`:

```java
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
```

Create `src/main/java/com/kousenit/shopping/dto/ProductResponse.java`:

```java
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
    LocalDateTime updatedAt
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
            null, // Add timestamps to Product entity if needed
            null
        );
    }
}
```

Create `src/main/java/com/kousenit/shopping/dto/StockUpdateRequest.java`:

```java
package com.kousenit.shopping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    Integer quantity
) {}
```

Create `src/main/java/com/kousenit/shopping/dto/PriceUpdateRequest.java`:

```java
package com.kousenit.shopping.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PriceUpdateRequest(
    @NotNull(message = "Price multiplier is required")
    @DecimalMin(value = "0.01", message = "Price multiplier must be positive")
    BigDecimal priceMultiplier
) {}
```

### Step 6.2: Create the REST Controller

Create `src/main/java/com/kousenit/shopping/controllers/ProductRestController.java`:

```java
package com.kousenit.shopping.controllers;

import com.kousenit.shopping.dto.*;
import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.services.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Validated
@CrossOrigin(origins = "*") // For development only
public class ProductRestController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductRestController.class);
    
    private final ProductService productService;
    
    @Autowired
    public ProductRestController(ProductService productService) {
        this.productService = productService;
    }
    
    // GET /api/v1/products - Get all products with pagination
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        logger.debug("Fetching products: page={}, size={}, sortBy={}, direction={}", 
                    page, size, sortBy, sortDirection);
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Product> products = productService.getAllProducts(pageable);
        Page<ProductResponse> response = products.map(ProductResponse::from);
        
        return ResponseEntity.ok(response);
    }
    
    // GET /api/v1/products/{id} - Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        logger.debug("Fetching product with ID: {}", id);
        
        return productService.getProductById(id)
            .map(ProductResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // POST /api/v1/products - Create new product
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        logger.info("Creating new product: {}", request.name());
        
        Product product = mapToEntity(request);
        Product created = productService.createProduct(product);
        ProductResponse response = ProductResponse.from(created);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        
        return ResponseEntity.created(location).body(response);
    }
    
    // PUT /api/v1/products/{id} - Update entire product
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductRequest request) {
        
        logger.info("Updating product with ID: {}", id);
        
        Product updates = mapToEntity(request);
        Product updated = productService.updateProduct(id, updates);
        ProductResponse response = ProductResponse.from(updated);
        
        return ResponseEntity.ok(response);
    }
    
    // PATCH /api/v1/products/{id} - Partial update
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> partialUpdateProduct(
            @PathVariable Long id, 
            @RequestBody ProductRequest request) {
        
        logger.info("Partially updating product with ID: {}", id);
        
        Product updates = mapToEntityPartial(request);
        Product updated = productService.updateProduct(id, updates);
        ProductResponse response = ProductResponse.from(updated);
        
        return ResponseEntity.ok(response);
    }
    
    // DELETE /api/v1/products/{id} - Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("Deleting product with ID: {}", id);
        
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    // GET /api/v1/products/search - Search products by name
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam String name) {
        
        logger.debug("Searching products by name: {}", name);
        
        List<Product> products = productService.searchProductsByName(name);
        List<ProductResponse> response = products.stream()
            .map(ProductResponse::from)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    // GET /api/v1/products/price-range - Get products in price range
    @GetMapping("/price-range")
    public ResponseEntity<List<ProductResponse>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        
        logger.debug("Fetching products in price range: {} - {}", minPrice, maxPrice);
        
        List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        List<ProductResponse> response = products.stream()
            .map(ProductResponse::from)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    // GET /api/v1/products/under-price - Get products under price
    @GetMapping("/under-price")
    public ResponseEntity<List<ProductResponse>> getProductsUnderPrice(
            @RequestParam BigDecimal maxPrice) {
        
        logger.debug("Fetching products under price: {}", maxPrice);
        
        List<Product> products = productService.getProductsUnderPrice(maxPrice);
        List<ProductResponse> response = products.stream()
            .map(ProductResponse::from)
            .toList();
        
        return ResponseEntity.ok(response);
    }
    
    // GET /api/v1/products/most-expensive - Get most expensive product
    @GetMapping("/most-expensive")
    public ResponseEntity<ProductResponse> getMostExpensiveProduct() {
        logger.debug("Fetching most expensive product");
        
        return productService.getMostExpensiveProduct()
            .map(ProductResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // POST /api/v1/products/batch - Create multiple products
    @PostMapping("/batch")
    public ResponseEntity<List<ProductResponse>> createProducts(
            @Valid @RequestBody List<ProductRequest> requests) {
        
        logger.info("Creating {} products in batch", requests.size());
        
        List<Product> products = requests.stream()
            .map(this::mapToEntity)
            .toList();
        
        List<Product> created = productService.createProducts(products);
        List<ProductResponse> response = created.stream()
            .map(ProductResponse::from)
            .toList();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // PUT /api/v1/products/{id}/stock - Update stock quantity
    @PutMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        
        logger.info("Updating stock for product {}: {}", id, request.quantity());
        
        Product updated = productService.updateStock(id, request.quantity());
        ProductResponse response = ProductResponse.from(updated);
        
        return ResponseEntity.ok(response);
    }
    
    // POST /api/v1/products/{id}/reserve-stock - Reserve stock
    @PostMapping("/{id}/reserve-stock")
    public ResponseEntity<ProductResponse> reserveStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        
        logger.info("Reserving stock for product {}: {}", id, request.quantity());
        
        Product updated = productService.reserveStock(id, request.quantity());
        ProductResponse response = ProductResponse.from(updated);
        
        return ResponseEntity.ok(response);
    }
    
    // POST /api/v1/products/{id}/release-stock - Release reserved stock
    @PostMapping("/{id}/release-stock")
    public ResponseEntity<ProductResponse> releaseStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        
        logger.info("Releasing stock for product {}: {}", id, request.quantity());
        
        Product updated = productService.releaseStock(id, request.quantity());
        ProductResponse response = ProductResponse.from(updated);
        
        return ResponseEntity.ok(response);
    }
    
    // GET /api/v1/products/{id}/stock-check - Check if product is in stock
    @GetMapping("/{id}/stock-check")
    public ResponseEntity<Boolean> checkStock(
            @PathVariable Long id,
            @RequestParam int requiredQuantity) {
        
        logger.debug("Checking stock for product {}: required {}", id, requiredQuantity);
        
        boolean inStock = productService.isProductInStock(id, requiredQuantity);
        return ResponseEntity.ok(inStock);
    }
    
    // PUT /api/v1/products/batch/prices - Update prices for multiple products
    @PutMapping("/batch/prices")
    public ResponseEntity<Void> updatePrices(
            @RequestParam List<Long> productIds,
            @Valid @RequestBody PriceUpdateRequest request) {
        
        logger.info("Updating prices for {} products with multiplier: {}", 
                   productIds.size(), request.priceMultiplier());
        
        productService.updatePrices(productIds, request.priceMultiplier());
        return ResponseEntity.noContent().build();
    }
    
    // DELETE /api/v1/products/batch - Delete multiple products
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteProducts(@RequestParam List<Long> productIds) {
        logger.info("Deleting {} products", productIds.size());
        
        productService.deleteProducts(productIds);
        return ResponseEntity.noContent().build();
    }
    
    // Mapping helper methods
    private Product mapToEntity(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setPrice(request.price());
        product.setDescription(request.description());
        product.setQuantity(request.quantity() != null ? request.quantity() : 0);
        product.setSku(request.sku());
        product.setContactEmail(request.contactEmail());
        return product;
    }
    
    private Product mapToEntityPartial(ProductRequest request) {
        Product product = new Product();
        // Only set non-null values for partial updates
        if (request.name() != null) product.setName(request.name());
        if (request.price() != null) product.setPrice(request.price());
        if (request.description() != null) product.setDescription(request.description());
        if (request.quantity() != null) product.setQuantity(request.quantity());
        if (request.sku() != null) product.setSku(request.sku());
        if (request.contactEmail() != null) product.setContactEmail(request.contactEmail());
        return product;
    }
}
```

### Step 6.3: Create Controller Tests

Create `src/test/java/com/kousenit/shopping/controllers/ProductRestControllerTest.java`:

```java
package com.kousenit.shopping.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kousenit.shopping.dto.ProductRequest;
import com.kousenit.shopping.dto.StockUpdateRequest;
import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.exceptions.ProductNotFoundException;
import com.kousenit.shopping.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductRestController.class)
class ProductRestControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private ProductService productService;
    
    private Product sampleProduct;
    private ProductRequest sampleRequest;
    
    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("Test Product");
        sampleProduct.setPrice(new BigDecimal("99.99"));
        sampleProduct.setDescription("A test product");
        sampleProduct.setQuantity(10);
        sampleProduct.setSku("TST-123456");
        sampleProduct.setContactEmail("test@example.com");
        
        sampleRequest = new ProductRequest(
            "Test Product",
            new BigDecimal("99.99"),
            "A test product",
            10,
            "TST-123456",
            "test@example.com"
        );
    }
    
    @Test
    void testGetAllProducts() throws Exception {
        // Given
        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct));
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productPage);
        
        // When/Then
        mockMvc.perform(get("/api/v1/products"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Test Product"))
            .andExpect(jsonPath("$.content[0].price").value(99.99));
    }
    
    @Test
    void testGetProductById() throws Exception {
        // Given
        when(productService.getProductById(1L)).thenReturn(Optional.of(sampleProduct));
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Product"))
            .andExpect(jsonPath("$.price").value(99.99));
    }
    
    @Test
    void testGetProductByIdNotFound() throws Exception {
        // Given
        when(productService.getProductById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/999"))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void testCreateProduct() throws Exception {
        // Given
        when(productService.createProduct(any(Product.class))).thenReturn(sampleProduct);
        
        // When/Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Product"));
        
        verify(productService).createProduct(any(Product.class));
    }
    
    @Test
    void testCreateProductWithValidationErrors() throws Exception {
        // Given - invalid request with blank name
        ProductRequest invalidRequest = new ProductRequest(
            "", // blank name
            new BigDecimal("99.99"),
            "A test product",
            10,
            "TST-123456",
            "test@example.com"
        );
        
        // When/Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
        
        verify(productService, never()).createProduct(any());
    }
    
    @Test
    void testUpdateProduct() throws Exception {
        // Given
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(sampleProduct);
        
        // When/Then
        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpected(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Product"));
        
        verify(productService).updateProduct(eq(1L), any(Product.class));
    }
    
    @Test
    void testUpdateProductNotFound() throws Exception {
        // Given
        when(productService.updateProduct(eq(999L), any(Product.class)))
            .thenThrow(new ProductNotFoundException(999L));
        
        // When/Then
        mockMvc.perform(put("/api/v1/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void testDeleteProduct() throws Exception {
        // Given
        doNothing().when(productService).deleteProduct(1L);
        
        // When/Then
        mockMvc.perform(delete("/api/v1/products/1"))
            .andExpect(status().isNoContent());
        
        verify(productService).deleteProduct(1L);
    }
    
    @Test
    void testDeleteProductNotFound() throws Exception {
        // Given
        doThrow(new ProductNotFoundException(999L)).when(productService).deleteProduct(999L);
        
        // When/Then
        mockMvc.perform(delete("/api/v1/products/999"))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void testSearchProducts() throws Exception {
        // Given
        when(productService.searchProductsByName("Test")).thenReturn(List.of(sampleProduct));
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/search?name=Test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].name").value("Test Product"));
    }
    
    @Test
    void testGetProductsByPriceRange() throws Exception {
        // Given
        when(productService.getProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
            .thenReturn(List.of(sampleProduct));
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/price-range?minPrice=50.00&maxPrice=150.00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].price").value(99.99));
    }
    
    @Test
    void testUpdateStock() throws Exception {
        // Given
        StockUpdateRequest stockRequest = new StockUpdateRequest(20);
        sampleProduct.setQuantity(20);
        when(productService.updateStock(1L, 20)).thenReturn(sampleProduct);
        
        // When/Then
        mockMvc.perform(put("/api/v1/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(20));
    }
    
    @Test
    void testReserveStock() throws Exception {
        // Given
        StockUpdateRequest stockRequest = new StockUpdateRequest(5);
        sampleProduct.setQuantity(5);
        when(productService.reserveStock(1L, 5)).thenReturn(sampleProduct);
        
        // When/Then
        mockMvc.perform(post("/api/v1/products/1/reserve-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(5));
    }
    
    @Test
    void testCheckStock() throws Exception {
        // Given
        when(productService.isProductInStock(1L, 5)).thenReturn(true);
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/1/stock-check?requiredQuantity=5"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
    
    @Test
    void testCreateProductsBatch() throws Exception {
        // Given
        List<ProductRequest> requests = List.of(sampleRequest);
        when(productService.createProducts(any())).thenReturn(List.of(sampleProduct));
        
        // When/Then
        mockMvc.perform(post("/api/v1/products/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].name").value("Test Product"));
    }
    
    @Test
    void testGetMostExpensiveProduct() throws Exception {
        // Given
        when(productService.getMostExpensiveProduct()).thenReturn(Optional.of(sampleProduct));
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/most-expensive"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Product"));
    }
    
    @Test
    void testGetMostExpensiveProductNotFound() throws Exception {
        // Given
        when(productService.getMostExpensiveProduct()).thenReturn(Optional.empty());
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/most-expensive"))
            .andExpect(status().isNotFound());
    }
}
```

### Step 6.4: Integration Tests

Create `src/test/java/com/kousenit/shopping/controllers/ProductRestControllerIntegrationTest.java`:

```java
package com.kousenit.shopping.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kousenit.shopping.dto.ProductRequest;
import com.kousenit.shopping.dto.StockUpdateRequest;
import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ProductRestControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProductRepository productRepository;
    
    private ProductRequest sampleRequest;
    
    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        
        sampleRequest = new ProductRequest(
            "Integration Test Product",
            new BigDecimal("199.99"),
            "An integration test product",
            15,
            "INT-654321",
            "integration@test.com"
        );
    }
    
    @Test
    void testCompleteProductLifecycle() throws Exception {
        // Create product
        String createResponse = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.name").value("Integration Test Product"))
            .andExpect(jsonPath("$.price").value(199.99))
            .andReturn().getResponse().getContentAsString();
        
        // Extract ID from response
        Long productId = objectMapper.readTree(createResponse).get("id").asLong();
        
        // Get product
        mockMvc.perform(get("/api/v1/products/" + productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Integration Test Product"));
        
        // Update product
        ProductRequest updateRequest = new ProductRequest(
            "Updated Integration Product",
            new BigDecimal("249.99"),
            "Updated description",
            20,
            "UPD-654321",
            "updated@test.com"
        );
        
        mockMvc.perform(put("/api/v1/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Integration Product"))
            .andExpect(jsonPath("$.price").value(249.99));
        
        // Delete product
        mockMvc.perform(delete("/api/v1/products/" + productId))
            .andExpect(status().isNoContent());
        
        // Verify deletion
        mockMvc.perform(get("/api/v1/products/" + productId))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void testStockManagement() throws Exception {
        // Create product
        String createResponse = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        
        Long productId = objectMapper.readTree(createResponse).get("id").asLong();
        
        // Update stock
        StockUpdateRequest stockUpdate = new StockUpdateRequest(25);
        mockMvc.perform(put("/api/v1/products/" + productId + "/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockUpdate)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(25));
        
        // Reserve stock
        StockUpdateRequest reserveRequest = new StockUpdateRequest(10);
        mockMvc.perform(post("/api/v1/products/" + productId + "/reserve-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(15));
        
        // Check stock
        mockMvc.perform(get("/api/v1/products/" + productId + "/stock-check?requiredQuantity=10"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
        
        mockMvc.perform(get("/api/v1/products/" + productId + "/stock-check?requiredQuantity=20"))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
        
        // Release stock
        StockUpdateRequest releaseRequest = new StockUpdateRequest(5);
        mockMvc.perform(post("/api/v1/products/" + productId + "/release-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(releaseRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(20));
    }
    
    @Test
    void testSearchAndFilter() throws Exception {
        // Create test products
        ProductRequest product1 = new ProductRequest("Apple iPhone", new BigDecimal("999.99"), 
                                                    "Smartphone", 10, "APL-001", "sales@apple.com");
        ProductRequest product2 = new ProductRequest("Samsung Galaxy", new BigDecimal("799.99"), 
                                                    "Smartphone", 15, "SAM-001", "sales@samsung.com");
        ProductRequest product3 = new ProductRequest("Apple iPad", new BigDecimal("599.99"), 
                                                    "Tablet", 8, "APL-002", "sales@apple.com");
        
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product1)))
            .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product2)))
            .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product3)))
            .andExpect(status().isCreated());
        
        // Search by name
        mockMvc.perform(get("/api/v1/products/search?name=Apple"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));
        
        // Filter by price range
        mockMvc.perform(get("/api/v1/products/price-range?minPrice=600&maxPrice=900"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));
        
        // Get products under price
        mockMvc.perform(get("/api/v1/products/under-price?maxPrice=800"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));
        
        // Get most expensive
        mockMvc.perform(get("/api/v1/products/most-expensive"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Apple iPhone"));
    }
    
    @Test
    void testPaginationAndSorting() throws Exception {
        // Create multiple products for pagination testing
        for (int i = 1; i <= 25; i++) {
            ProductRequest product = new ProductRequest(
                "Product " + i,
                new BigDecimal(String.valueOf(100 + i)),
                "Description " + i,
                i,
                String.format("PRD-%06d", i),
                "test" + i + "@example.com"
            );
            
            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated());
        }
        
        // Test pagination
        mockMvc.perform(get("/api/v1/products?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(10))
            .andExpect(jsonPath("$.totalElements").value(25))
            .andExpect(jsonPath("$.totalPages").value(3));
        
        // Test sorting by name
        mockMvc.perform(get("/api/v1/products?page=0&size=5&sortBy=name&sortDirection=desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Product 9"));
        
        // Test sorting by price
        mockMvc.perform(get("/api/v1/products?page=0&size=5&sortBy=price&sortDirection=asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].price").value(101.0));
    }
}
```

### Step 6.5: Run Controller Tests

```bash
# Run controller unit tests
./gradlew test --tests ProductRestControllerTest

# Run controller integration tests
./gradlew test --tests ProductRestControllerIntegrationTest

# Run all tests
./gradlew test

# Start the application to test REST API manually
./gradlew bootRun
```

### Step 6.6: Manual API Testing

With the application running, you can test the API with curl or Postman:

```bash
# Get all products with pagination
curl "http://localhost:8080/api/v1/products?page=0&size=5"

# Get product by ID
curl "http://localhost:8080/api/v1/products/1"

# Create a new product
curl -X POST "http://localhost:8080/api/v1/products" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Test Product",
       "price": 99.99,
       "description": "A test product",
       "quantity": 10,
       "sku": "TST-123456",
       "contactEmail": "test@example.com"
     }'

# Search products
curl "http://localhost:8080/api/v1/products/search?name=MacBook"

# Get products in price range
curl "http://localhost:8080/api/v1/products/price-range?minPrice=100&maxPrice=500"

# Update stock
curl -X PUT "http://localhost:8080/api/v1/products/1/stock" \
     -H "Content-Type: application/json" \
     -d '{"quantity": 25}'

# Reserve stock
curl -X POST "http://localhost:8080/api/v1/products/1/reserve-stock" \
     -H "Content-Type: application/json" \
     -d '{"quantity": 5}'
```

**Key Learning Points:**
- **REST API Design**: Proper HTTP methods, status codes, and resource naming
- **DTO Pattern**: Separating API contracts from internal entities
- **Validation Integration**: @Valid annotation with custom validation messages  
- **ResponseEntity**: Control over HTTP responses and headers
- **Pagination and Sorting**: Spring Data pagination support
- **Exception Handling**: Automatic mapping of service exceptions to HTTP status codes
- **MockMVC Testing**: Testing controllers without starting the full server
- **Integration Testing**: End-to-end API testing with real database operations

## Lab 7: Add Global Exception Handling with @RestControllerAdvice

**Objective**: Implement centralized exception handling using @RestControllerAdvice with modern ProblemDetail responses for consistent error handling across the API.

### Step 7.1: Create Error Response DTOs

First, create DTOs for standardized error responses:

Create `src/main/java/com/kousenit/shopping/dto/ApiError.java`:

```java
package com.kousenit.shopping.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    HttpStatus status,
    int statusCode,
    String error,
    String message,
    String path,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp,
    List<ValidationError> validationErrors,
    Map<String, Object> details
) {
    
    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(
            status,
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            LocalDateTime.now(),
            null,
            null
        );
    }
    
    public static ApiError withValidationErrors(HttpStatus status, String message, 
                                               String path, List<ValidationError> validationErrors) {
        return new ApiError(
            status,
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            LocalDateTime.now(),
            validationErrors,
            null
        );
    }
    
    public static ApiError withDetails(HttpStatus status, String message, 
                                      String path, Map<String, Object> details) {
        return new ApiError(
            status,
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            LocalDateTime.now(),
            null,
            details
        );
    }
}
```

Create `src/main/java/com/kousenit/shopping/dto/ValidationError.java`:

```java
package com.kousenit.shopping.dto;

public record ValidationError(
    String field,
    Object rejectedValue,
    String message,
    String code
) {
    
    public static ValidationError of(String field, Object rejectedValue, String message) {
        return new ValidationError(field, rejectedValue, message, null);
    }
    
    public static ValidationError of(String field, Object rejectedValue, String message, String code) {
        return new ValidationError(field, rejectedValue, message, code);
    }
}
```

### Step 7.2: Create the Global Exception Handler

Create `src/main/java/com/kousenit/shopping/controllers/GlobalExceptionHandler.java`:

```java
package com.kousenit.shopping.controllers;

import com.kousenit.shopping.dto.ApiError;
import com.kousenit.shopping.dto.ValidationError;
import com.kousenit.shopping.exceptions.InsufficientStockException;
import com.kousenit.shopping.exceptions.ProductNotFoundException;
import com.kousenit.shopping.exceptions.ProductValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String INVALID_REQUEST = "Invalid request";
    
    // Domain-specific exceptions
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleProductNotFoundException(
            ProductNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("Product not found: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setType(URI.create("https://api.shopping.com/problems/product-not-found"));
        problemDetail.setTitle("Product Not Found");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        if (ex.getProductId() != null) {
            problemDetail.setProperty("productId", ex.getProductId());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientStockException(
            InsufficientStockException ex, HttpServletRequest request) {
        
        logger.warn("Insufficient stock: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://api.shopping.com/problems/insufficient-stock"));
        problemDetail.setTitle("Insufficient Stock");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("productId", ex.getProductId());
        problemDetail.setProperty("requestedQuantity", ex.getRequestedQuantity());
        problemDetail.setProperty("availableQuantity", ex.getAvailableQuantity());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    @ExceptionHandler(ProductValidationException.class)
    public ResponseEntity<ProblemDetail> handleProductValidationException(
            ProductValidationException ex, HttpServletRequest request) {
        
        logger.warn("Product validation error: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://api.shopping.com/problems/validation-error"));
        problemDetail.setTitle("Validation Error");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        if (ex.getField() != null) {
            problemDetail.setProperty("field", ex.getField());
            problemDetail.setProperty("rejectedValue", ex.getValue());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    // Bean validation exceptions
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("Validation failed for request: {}", ex.getMessage());
        
        List<ValidationError> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::mapFieldError)
            .collect(Collectors.toList());
        
        // Add global errors if any
        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            validationErrors.add(ValidationError.of(
                error.getObjectName(),
                null,
                error.getDefaultMessage(),
                error.getCode()
            ));
        });
        
        ApiError apiError = ApiError.withValidationErrors(
            HttpStatus.BAD_REQUEST,
            VALIDATION_FAILED,
            request.getRequestURI(),
            validationErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        logger.warn("Constraint violation: {}", ex.getMessage());
        
        List<ValidationError> validationErrors = ex.getConstraintViolations()
            .stream()
            .map(this::mapConstraintViolation)
            .collect(Collectors.toList());
        
        ApiError apiError = ApiError.withValidationErrors(
            HttpStatus.BAD_REQUEST,
            VALIDATION_FAILED,
            request.getRequestURI(),
            validationErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
    
    // Request processing exceptions
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        logger.warn("Malformed JSON request: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Malformed JSON request");
        problemDetail.setType(URI.create("https://api.shopping.com/problems/malformed-request"));
        problemDetail.setTitle("Malformed Request");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logger.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
            ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, message);
        problemDetail.setType(URI.create("https://api.shopping.com/problems/type-mismatch"));
        problemDetail.setTitle("Type Mismatch");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("parameter", ex.getName());
        problemDetail.setProperty("rejectedValue", ex.getValue());
        problemDetail.setProperty("expectedType", ex.getRequiredType().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        logger.warn("Missing required parameter: {}", ex.getParameterName());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, message);
        problemDetail.setType(URI.create("https://api.shopping.com/problems/missing-parameter"));
        problemDetail.setTitle("Missing Parameter");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("parameter", ex.getParameterName());
        problemDetail.setProperty("parameterType", ex.getParameterType());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, "The requested resource was not found");
        problemDetail.setType(URI.create("https://api.shopping.com/problems/resource-not-found"));
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
    
    // Database exceptions
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data integrity violation. This operation conflicts with existing data constraints.";
        
        // Try to provide more specific messages for common violations
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Unique index or primary key violation")) {
                message = "A record with this information already exists.";
            } else if (ex.getMessage().contains("constraint")) {
                message = "This operation violates a data constraint.";
            }
        }
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, message);
        problemDetail.setType(URI.create("https://api.shopping.com/problems/data-integrity"));
        problemDetail.setTitle("Data Integrity Violation");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }
    
    // Generic exceptions
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create("https://api.shopping.com/problems/illegal-argument"));
        problemDetail.setTitle("Illegal Argument");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "An unexpected error occurred. Please try again later.");
        problemDetail.setType(URI.create("https://api.shopping.com/problems/internal-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        
        // Don't expose internal error details in production
        if (logger.isDebugEnabled()) {
            problemDetail.setProperty("debugMessage", ex.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
    
    // Helper methods
    
    private ValidationError mapFieldError(FieldError fieldError) {
        return ValidationError.of(
            fieldError.getField(),
            fieldError.getRejectedValue(),
            fieldError.getDefaultMessage(),
            fieldError.getCode()
        );
    }
    
    private ValidationError mapConstraintViolation(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        return ValidationError.of(
            field,
            violation.getInvalidValue(),
            violation.getMessage(),
            null
        );
    }
}
```

### Step 7.3: Test the Exception Handler

Create `src/test/java/com/kousenit/shopping/controllers/GlobalExceptionHandlerTest.java`:

```java
package com.kousenit.shopping.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kousenit.shopping.dto.ProductRequest;
import com.kousenit.shopping.exceptions.InsufficientStockException;
import com.kousenit.shopping.exceptions.ProductNotFoundException;
import com.kousenit.shopping.exceptions.ProductValidationException;
import com.kousenit.shopping.services.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ProductRestController.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private ProductService productService;
    
    @Test
    void testProductNotFoundExceptionHandler() throws Exception {
        // Given
        when(productService.getProductById(999L))
            .thenThrow(new ProductNotFoundException(999L));
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/999"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/product-not-found"))
            .andExpect(jsonPath("$.title").value("Product Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Product not found with id: 999"))
            .andExpect(jsonPath("$.productId").value(999))
            .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void testInsufficientStockExceptionHandler() throws Exception {
        // Given
        when(productService.reserveStock(1L, 50))
            .thenThrow(new InsufficientStockException(1L, 50, 10));
        
        ProductRequest request = new ProductRequest(null, null, null, 50, null, null);
        
        // When/Then
        mockMvc.perform(post("/api/v1/products/1/reserve-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/insufficient-stock"))
            .andExpect(jsonPath("$.title").value("Insufficient Stock"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.productId").value(1))
            .andExpect(jsonPath("$.requestedQuantity").value(50))
            .andExpect(jsonPath("$.availableQuantity").value(10));
    }
    
    @Test
    void testProductValidationExceptionHandler() throws Exception {
        // Given
        when(productService.createProduct(any()))
            .thenThrow(new ProductValidationException("price", BigDecimal.ZERO, "Price must be positive"));
        
        ProductRequest invalidRequest = new ProductRequest(
            "Test Product",
            BigDecimal.ZERO,
            "Description",
            10,
            "TST-123456",
            "test@example.com"
        );
        
        // When/Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/validation-error"))
            .andExpect(jsonPath("$.title").value("Validation Error"))
            .andExpect(jsonPath("$.field").value("price"))
            .andExpect(jsonPath("$.rejectedValue").value(0));
    }
    
    @Test
    void testMethodArgumentNotValidExceptionHandler() throws Exception {
        // Given - request with validation errors
        ProductRequest invalidRequest = new ProductRequest(
            "", // blank name - violates @NotBlank
            new BigDecimal("-10.00"), // negative price - violates @DecimalMin
            "A".repeat(501), // too long description - violates @Size
            -5, // negative quantity - violates @Min
            "INVALID-SKU", // invalid SKU format - violates @Pattern
            "not-an-email" // invalid email - violates @Email
        );
        
        // When/Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.statusCode").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.validationErrors").isArray())
            .andExpect(jsonPath("$.validationErrors", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.validationErrors[*].field", hasItems("name", "price", "description", "quantity", "sku", "contactEmail")));
    }
    
    @Test
    void testHttpMessageNotReadableExceptionHandler() throws Exception {
        // Given - malformed JSON
        String malformedJson = "{ invalid json }";
        
        // When/Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/malformed-request"))
            .andExpect(jsonPath("$.title").value("Malformed Request"))
            .andExpect(jsonPath("$.detail").value("Malformed JSON request"));
    }
    
    @Test
    void testMethodArgumentTypeMismatchExceptionHandler() throws Exception {
        // Given - invalid ID type (string instead of long)
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/invalid-id"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/type-mismatch"))
            .andExpect(jsonPath("$.title").value("Type Mismatch"))
            .andExpect(jsonPath("$.parameter").value("id"))
            .andExpect(jsonPath("$.rejectedValue").value("invalid-id"))
            .andExpect(jsonPath("$.expectedType").value("Long"));
    }
    
    @Test
    void testMissingServletRequestParameterExceptionHandler() throws Exception {
        // Given - missing required parameter
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/price-range?minPrice=100"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/missing-parameter"))
            .andExpect(jsonPath("$.title").value("Missing Parameter"))
            .andExpect(jsonPath("$.parameter").value("maxPrice"));
    }
    
    @Test
    void testNoResourceFoundExceptionHandler() throws Exception {
        // Given - non-existent endpoint
        
        // When/Then
        mockMvc.perform(get("/api/v1/non-existent-endpoint"))
            .andExpect(status().isNotFound())
            .andExpected(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/resource-not-found"))
            .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }
    
    @Test
    void testGenericExceptionHandler() throws Exception {
        // Given
        when(productService.getProductById(anyLong()))
            .thenThrow(new RuntimeException("Unexpected error"));
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/1"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/internal-error"))
            .andExpect(jsonPath("$.title").value("Internal Server Error"))
            .andExpect(jsonPath("$.status").value(500))
            .andExpected(jsonPath("$.detail").value("An unexpected error occurred. Please try again later."));
    }
}
```

### Step 7.4: Integration Tests for Exception Handling

Create `src/test/java/com/kousenit/shopping/controllers/ExceptionHandlingIntegrationTest.java`:

```java
package com.kousenit.shopping.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kousenit.shopping.dto.ProductRequest;
import com.kousenit.shopping.dto.StockUpdateRequest;
import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ExceptionHandlingIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProductRepository productRepository;
    
    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }
    
    @Test
    void testRealProductNotFoundScenario() throws Exception {
        // Verify database is empty
        assertThat(productRepository.count()).isEqualTo(0);
        
        // When/Then
        mockMvc.perform(get("/api/v1/products/999"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/product-not-found"))
            .andExpect(jsonPath("$.productId").value(999));
    }
    
    @Test
    void testRealInsufficientStockScenario() throws Exception {
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
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/insufficient-stock"))
            .andExpect(jsonPath("$.productId").value(saved.getId()))
            .andExpect(jsonPath("$.requestedQuantity").value(10))
            .andExpect(jsonPath("$.availableQuantity").value(5));
    }
    
    @Test
    void testRealValidationErrorScenario() throws Exception {
        // Create product with multiple validation errors
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
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpected(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.validationErrors").isArray())
            .andExpect(jsonPath("$.validationErrors[?(@.field == 'name')]").exists())
            .andExpect(jsonPath("$.validationErrors[?(@.field == 'price')]").exists())
            .andExpect(jsonPath("$.validationErrors[?(@.field == 'quantity')]").exists())
            .andExpect(jsonPath("$.validationErrors[?(@.field == 'sku')]").exists())
            .andExpect(jsonPath("$.validationErrors[?(@.field == 'contactEmail')]").exists());
    }
    
    @Test
    void testRealDuplicateSkuScenario() throws Exception {
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
            .andExpect(jsonPath("$.type").value("https://api.shopping.com/problems/data-integrity"))
            .andExpect(jsonPath("$.title").value("Data Integrity Violation"));
    }
    
    @Test
    void testSuccessfulRequestAfterErrorHandling() throws Exception {
        // First, make a request that causes an error
        mockMvc.perform(get("/api/v1/products/999"))
            .andExpect(status().isNotFound());
        
        // Then, make a successful request to verify the application still works
        ProductRequest validRequest = new ProductRequest(
            "Valid Product",
            new BigDecimal("99.99"),
            "A valid product",
            10,
            "VAL-123456",
            "valid@example.com"
        );
        
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Valid Product"));
        
        // Verify the product was actually created
        assertThat(productRepository.count()).isEqualTo(1);
    }
}
```

### Step 7.5: Run Exception Handler Tests

```bash
# Run exception handler unit tests
./gradlew test --tests GlobalExceptionHandlerTest

# Run exception handling integration tests
./gradlew test --tests ExceptionHandlingIntegrationTest

# Run all tests to verify everything still works
./gradlew test

# Start the application to test error handling manually
./gradlew bootRun
```

### Step 7.6: Manual Error Testing

Test the exception handling with curl:

```bash
# Test ProductNotFoundException
curl "http://localhost:8080/api/v1/products/999"

# Test validation errors
curl -X POST "http://localhost:8080/api/v1/products" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "",
       "price": -10,
       "quantity": -5,
       "sku": "INVALID",
       "contactEmail": "not-an-email"
     }'

# Test malformed JSON
curl -X POST "http://localhost:8080/api/v1/products" \
     -H "Content-Type: application/json" \
     -d '{ invalid json'

# Test type mismatch
curl "http://localhost:8080/api/v1/products/not-a-number"

# Test missing parameter
curl "http://localhost:8080/api/v1/products/price-range?minPrice=100"

# Test non-existent endpoint
curl "http://localhost:8080/api/v1/non-existent"
```

### Step 7.7: Complete application.yml Configuration

Update `src/main/resources/application.yml` with the final production-ready configuration:

```yaml
spring:
  application:
    name: shopping
  
  datasource:
    url: jdbc:h2:mem:shopping
    driver-class-name: org.h2.Driver
    username: sa
    password: 
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 20000
  
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: false
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        generate_statistics: false
        jdbc:
          batch_size: 25
        order_inserts: true
        order_updates: true
        
  mvc:
    problemdetails:
      enabled: true
  
  web:
    problemdetails:
      enabled: true

server:
  port: 8080
  error:
    include-stacktrace: never
    include-exception: false
    include-message: always
    include-binding-errors: always
    whitelabel:
      enabled: false

logging:
  level:
    root: INFO
    com.kousenit.shopping: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql: TRACE
    org.springframework.data: DEBUG
    
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
  endpoint:
    health:
      show-details: always

---
# Test profile configuration
spring:
  config:
    activate:
      on-profile: test
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        use_sql_comments: false
  
logging:
  level:
    root: WARN
    com.kousenit.shopping: INFO
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
    org.hibernate.type.descriptor.sql: WARN
    org.springframework.data: WARN
```

**Key Learning Points:**
- **@RestControllerAdvice**: Global exception handling across all controllers
- **ProblemDetail**: RFC 7807 standard for HTTP API error responses
- **Exception Hierarchy**: Handling both domain-specific and framework exceptions
- **Structured Error Responses**: Consistent error format with validation details
- **Logging Strategy**: Appropriate log levels for different exception types
- **Security Considerations**: Not exposing sensitive internal details
- **Error Testing**: Comprehensive testing of error scenarios
- **Client-Friendly Errors**: Meaningful error messages for API consumers
