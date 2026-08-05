# Advanced Spring Boot Labs

## Prerequisites

- **Java 21** (standardized across all projects)
- **Spring Boot 4.1.0**
- **Gradle 9.6.1**
- **IDE** with Spring Boot support (IntelliJ IDEA, VS Code, or Eclipse)
- **Basic Spring Boot knowledge** (complete basic labs first)

> [!NOTE]
> In the two-day course format these labs are an **optional self-study and reference track**. The class covers the basic labs (labs.md); work through this document afterward at your own pace, or use it as a worked example of enterprise Spring Boot patterns. The completed solution is in the `shopping` project.

## Table of Contents

1. [Lab 1: Create the Product Entity](#lab-1-create-the-product-entity)
2. [Lab 2: Create the Repository Layer with Custom Queries](#lab-2-create-the-repository-layer-with-custom-queries)
3. [Lab 3: Create DTOs for API Boundaries](#lab-3-create-dtos-for-api-boundaries)
4. [Lab 4: Add Bean Validation and Custom Exceptions](#lab-4-add-bean-validation-and-custom-exceptions)
5. [Lab 5: Add Service Layer with Transaction Management](#lab-5-add-service-layer-with-transaction-management)
6. [Lab 6: Create REST Controller with Full CRUD Operations](#lab-6-create-rest-controller-with-full-crud-operations)
7. [Lab 7: Add Global Exception Handling with RFC 7807 ProblemDetail](#lab-7-add-global-exception-handling-with-rfc-7807-problemdetail)
8. [Lab 8: Database Initialization with CommandLineRunner](#lab-8-database-initialization-with-commandlinerunner)
9. [Lab 9: Configure Application with Production-Ready Settings](#lab-9-configure-application-with-production-ready-settings)
10. [Lab 10: Write Comprehensive Tests](#lab-10-write-comprehensive-tests)

## Overview

These advanced labs demonstrate enterprise-level Spring Boot patterns used in production applications. You'll build a complete shopping API with:

- **Progressive Entity Design** - Start with basic JPA, add validation in Lab 4
- **Bean Validation** - Comprehensive validation framework (Jakarta Validation)
- **DTO Pattern** - Separation between domain model and API contracts
- **Clean Service Layer** - Business logic without interface bloat
- **RESTful API Design** - Proper HTTP semantics and status codes
- **RFC 7807 Error Handling** - Standardized error responses with ProblemDetail
- **Transaction Management** - Proper use of @Transactional
- **Test Isolation** - Modern testing patterns with @DirtiesContext
- **Production Configuration** - Comprehensive application.yml with profiles

The labs follow a **progressive learning approach**: Lab 1 introduces basic JPA entities, and Lab 4 adds validation annotations to demonstrate Bean Validation as a distinct concept.

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
│   │   │               │   └── AppConfig.java
│   │   │               ├── controllers/
│   │   │               │   ├── ProductRestController.java
│   │   │               │   └── GlobalExceptionHandler.java
│   │   │               ├── dto/
│   │   │               │   ├── ProductRequest.java
│   │   │               │   ├── ProductResponse.java
│   │   │               │   ├── StockUpdateRequest.java
│   │   │               │   ├── ApiError.java
│   │   │               │   └── ValidationError.java
│   │   │               ├── entities/
│   │   │               │   └── Product.java
│   │   │               ├── exceptions/
│   │   │               │   ├── ProductNotFoundException.java
│   │   │               │   ├── InsufficientStockException.java
│   │   │               │   └── ProductValidationException.java
│   │   │               ├── repositories/
│   │   │               │   └── ProductRepository.java
│   │   │               └── services/
│   │   │                   └── ProductService.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── com/
│               └── kousenit/
│                   └── shopping/
│                       ├── ShoppingApplicationIntegrationTest.java
│                       ├── controllers/
│                       │   └── ProductRestControllerTest.java
│                       ├── entities/
│                       │   └── ProductTest.java
│                       ├── repositories/
│                       │   └── ProductRepositoryTest.java
│                       └── services/
│                           └── ProductServiceTest.java
└── build.gradle
```

### Initial build.gradle

```gradle
plugins {
    id 'org.springframework.boot' version '4.1.0'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'java'
}

group = 'com.kousenit'
version = '0.0.1-SNAPSHOT'

// Java toolchain is inherited from root build.gradle (Java 21)

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
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    runtimeOnly 'com.h2database:h2'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

> [!NOTE]
> The root `build.gradle` configures Java 21 for all subprojects via the toolchain mechanism.

---

## Lab 1: Create the Product Entity

**Objective**: Create a basic JPA entity with Lombok, indexes, and business logic methods.

> **Note**: We'll add validation annotations in Lab 4 to demonstrate bean validation as a distinct concept.

### Step 1.1: Create the Product Entity

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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(unique = true, nullable = false)
    private String sku;

    private String contactEmail;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
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

**Key Features:**
- **Basic JPA Mapping** - `@Entity`, `@Table`, `@Column` annotations
- **Database Indexes** - Optimized queries for SKU (unique) and name lookups
- **Audit Fields** - Automatic timestamps using both JPA callbacks and database defaults
  - `@PrePersist`/`@PreUpdate` - JPA lifecycle callbacks set values when saving through the application
  - `columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"` - Database defaults allow manual SQL inserts via H2 console
- **Business Logic** - Stock management methods in the entity
- **Lombok** - Clean code with `@Data` (generates getters, setters, equals, hashCode, toString)

> **What's Missing?** Validation annotations! We'll add those in Lab 4 when we learn about Bean Validation.

### Step 1.2: Create Basic Entity Tests

Create `src/test/java/com/kousenit/shopping/entities/ProductTest.java`:

```java
package com.kousenit.shopping.entities;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void testHasStockReturnsTrue() {
        Product product = new Product();
        product.setQuantity(10);

        assertThat(product.hasStock(5)).isTrue();
    }

    @Test
    void testHasStockReturnsFalse() {
        Product product = new Product();
        product.setQuantity(3);

        assertThat(product.hasStock(5)).isFalse();
    }

    @Test
    void testDecrementStockSuccess() {
        Product product = new Product();
        product.setQuantity(10);

        product.decrementStock(3);

        assertThat(product.getQuantity()).isEqualTo(7);
    }

    @Test
    void testDecrementStockThrowsExceptionWhenInsufficientStock() {
        Product product = new Product();
        product.setQuantity(2);

        assertThatThrownBy(() -> product.decrementStock(5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot decrement stock by 5");
    }

    @Test
    void testIncrementStock() {
        Product product = new Product();
        product.setQuantity(10);

        product.incrementStock(5);

        assertThat(product.getQuantity()).isEqualTo(15);
    }
}
```

### Step 1.3: Run Tests

```bash
cd shopping
./gradlew test --tests ProductTest
```

[Back to Table of Contents](#table-of-contents)

---

## Lab 2: Create the Repository Layer with Custom Queries

**Objective**: Implement the data access layer using Spring Data JPA with custom queries.

### Step 2.1: Create the ProductRepository Interface

Create `src/main/java/com/kousenit/shopping/repositories/ProductRepository.java`:

```java
package com.kousenit.shopping.repositories;

import com.kousenit.shopping.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Derived query methods (Spring Data generates implementation)
    Optional<Product> findBySku(String sku);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByQuantityGreaterThan(Integer quantity);

    List<Product> findByNameContainingIgnoreCaseAndPriceBetween(
        String name, BigDecimal minPrice, BigDecimal maxPrice);

    boolean existsBySku(String sku);

    long countByQuantityLessThan(Integer quantity);

    // Custom JPQL queries
    @Query("SELECT p FROM Product p WHERE p.quantity < :threshold ORDER BY p.quantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    @Query("SELECT p FROM Product p WHERE p.price >= :minPrice ORDER BY p.price DESC")
    List<Product> findExpensiveProducts(@Param("minPrice") BigDecimal minPrice);

    // Modifying queries for batch operations
    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity - :amount WHERE p.id = :id")
    void decrementStock(@Param("id") Long id, @Param("amount") Integer amount);

    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity + :amount WHERE p.id = :id")
    void incrementStock(@Param("id") Long id, @Param("amount") Integer amount);

    // Native SQL query example
    @Query(value = "SELECT * FROM products WHERE price > :price AND quantity > 0 ORDER BY created_at DESC LIMIT :limit",
           nativeQuery = true)
    List<Product> findRecentProductsInStock(@Param("price") BigDecimal price, @Param("limit") int limit);
}
```

**Key Features:**
- **Derived Queries** - Method names generate SQL automatically
- **Custom JPQL** - @Query for complex business logic
- **Native SQL** - When you need database-specific features
- **Modifying Queries** - Batch operations with @Modifying
- **Type Safety** - Compile-time checking with method signatures

### Step 2.2: Create Repository Tests

Create `src/test/java/com/kousenit/shopping/repositories/ProductRepositoryTest.java`:

```java
package com.kousenit.shopping.repositories;

import com.kousenit.shopping.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
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
        laptop = new Product();
        laptop.setName("Gaming Laptop");
        laptop.setPrice(new BigDecimal("1299.99"));
        laptop.setDescription("High-performance laptop");
        laptop.setQuantity(5);
        laptop.setSku("LAP-123456");
        laptop.setContactEmail("sales@tech.com");

        mouse = new Product();
        mouse.setName("Wireless Mouse");
        mouse.setPrice(new BigDecimal("29.99"));
        mouse.setDescription("Ergonomic wireless mouse");
        mouse.setQuantity(50);
        mouse.setSku("MOU-123456");
        mouse.setContactEmail("sales@tech.com");

        keyboard = new Product();
        keyboard.setName("Mechanical Keyboard");
        keyboard.setPrice(new BigDecimal("89.99"));
        keyboard.setDescription("RGB mechanical keyboard");
        keyboard.setQuantity(20);
        keyboard.setSku("KEY-123456");
        keyboard.setContactEmail("sales@tech.com");

        entityManager.persist(laptop);
        entityManager.persist(mouse);
        entityManager.persist(keyboard);
        entityManager.flush();
    }

    @Test
    void testFindBySku() {
        Optional<Product> found = productRepository.findBySku("LAP-123456");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Gaming Laptop");
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
    void testFindLowStockProducts() {
        List<Product> lowStock = productRepository.findLowStockProducts(10);

        assertThat(lowStock).hasSize(1);
        assertThat(lowStock.get(0).getName()).isEqualTo("Gaming Laptop");
        assertThat(lowStock.get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void testFindExpensiveProducts() {
        List<Product> expensive = productRepository.findExpensiveProducts(new BigDecimal("100.00"));

        assertThat(expensive).hasSize(1);
        assertThat(expensive.get(0).getName()).isEqualTo("Gaming Laptop");
    }

    @Test
    void testExistsBySku() {
        boolean exists = productRepository.existsBySku("MOU-123456");
        assertThat(exists).isTrue();

        boolean notExists = productRepository.existsBySku("XXX-999999");
        assertThat(notExists).isFalse();
    }

    @Test
    void testCountByQuantityLessThan() {
        long count = productRepository.countByQuantityLessThan(10);
        assertThat(count).isEqualTo(1); // Only laptop has quantity < 10
    }
}
```

### Step 2.3: Run Repository Tests

```bash
./gradlew test --tests ProductRepositoryTest
```

[Back to Table of Contents](#table-of-contents)

---

## Lab 3: Create DTOs for API Boundaries

**Objective**: Separate the domain model from API contracts using Data Transfer Objects (DTOs) implemented as Java records.

**Why DTOs?**
- **Decoupling**: API contracts independent of database schema
- **Security**: Don't expose internal entity structure
- **Validation**: Different validation rules for input vs. domain
- **Immutability**: Records are perfect for request/response objects

> **Teaching Note**: DTOs include validation annotations even though we haven't added them to the entity yet (that comes in Lab 4). This demonstrates an important principle: **DTOs validate API input, entities represent domain state**. They can have different validation rules.

### Step 3.1: Create ProductRequest DTO

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

### Step 3.2: Create ProductResponse DTO

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
```

**Key Features:**
- **Static Factory Method** - Clean conversion from entity
- **Computed Fields** - `inStock` and `stockStatus` derived from quantity
- **Immutability** - Records are immutable by default

### Step 3.3: Create StockUpdateRequest DTO

Create `src/main/java/com/kousenit/shopping/dto/StockUpdateRequest.java`:

```java
package com.kousenit.shopping.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity
) {}
```

### Step 3.4: Create ValidationError DTO

Create `src/main/java/com/kousenit/shopping/dto/ValidationError.java`:

```java
package com.kousenit.shopping.dto;

public record ValidationError(
    String field,
    Object rejectedValue,
    String message,
    String code
) {
    public static ValidationError of(String field, Object rejectedValue, String message, String code) {
        return new ValidationError(field, rejectedValue, message, code);
    }
}
```

### Step 3.5: Create ApiError DTO

Create `src/main/java/com/kousenit/shopping/dto/ApiError.java`:

```java
package com.kousenit.shopping.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ValidationError> validationErrors
) {
    public static ApiError withValidationErrors(
            HttpStatus status,
            String message,
            String path,
            List<ValidationError> validationErrors) {
        return new ApiError(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            validationErrors
        );
    }
}
```

[Back to Table of Contents](#table-of-contents)

---

## Lab 4: Add Bean Validation and Custom Exceptions

**Objective**: Add comprehensive validation to the Product entity and create domain-specific exceptions.

This lab demonstrates the **Bean Validation** framework (Jakarta Validation) and shows how to create custom exceptions for business logic errors.

### Step 4.1: Add Bean Validation Annotations to Product Entity

Update `src/main/java/com/kousenit/shopping/entities/Product.java` to add validation:

```java
package com.kousenit.shopping.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;  // Add this import
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

    // Add validation annotations
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

    @PositiveOrZero(message = "Quantity must be greater than or equal to zero")
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

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
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

**Bean Validation Annotations Explained:**
- `@NotBlank` - String must not be null, empty, or whitespace-only
- `@NotNull` - Value must not be null
- `@Size` - String/collection size constraints
- `@DecimalMin`/`@DecimalMax` - Numeric range validation
- `@PositiveOrZero` - Number must be >= 0
- `@Pattern` - Regex pattern matching
- `@Email` - Valid email format

These annotations work with Spring Boot's `@Valid` annotation in controllers to automatically validate request data.

**Timestamp Strategy - Best of Both Worlds:**

Notice the `createdAt` and `updatedAt` fields use **two mechanisms**:
1. **`@PrePersist`/`@PreUpdate`** - JPA lifecycle callbacks set values when saving through the application
2. **`columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"`** - Database defaults for manual SQL inserts

This dual approach allows:
- ✅ Application saves through Spring Boot use JPA callbacks
- ✅ Manual SQL inserts via H2 console get database defaults
- ✅ No errors when experimenting with the database directly

### Step 4.2: Create ProductNotFoundException

Create `src/main/java/com/kousenit/shopping/exceptions/ProductNotFoundException.java`:

```java
package com.kousenit.shopping.exceptions;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
        this.productId = productId;
    }

    public ProductNotFoundException(String message) {
        super(message);
        this.productId = null;
    }
}
```

### Step 4.2: Create InsufficientStockException

Create `src/main/java/com/kousenit/shopping/exceptions/InsufficientStockException.java`:

```java
package com.kousenit.shopping.exceptions;

import lombok.Getter;

@Getter
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
}
```

### Step 4.3: Create ProductValidationException

Create `src/main/java/com/kousenit/shopping/exceptions/ProductValidationException.java`:

```java
package com.kousenit.shopping.exceptions;

import lombok.Getter;

@Getter
public class ProductValidationException extends RuntimeException {
    private final String field;
    private final Object value;

    public ProductValidationException(String field, Object value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }
}
```

**Why Lombok @Getter?**
- Generates getters automatically for exception fields
- Cleaner code without boilerplate
- Fields remain properly encapsulated (private final)

[Back to Table of Contents](#table-of-contents)

---

## Lab 5: Add Service Layer with Transaction Management

**Objective**: Create a service layer that encapsulates business logic, returns DTOs, and demonstrates proper transaction management.

**Modern Pattern**: No interfaces! Direct service classes are simpler for most applications.

### Step 5.1: Create ProductService

Create `src/main/java/com/kousenit/shopping/services/ProductService.java`:

```java
package com.kousenit.shopping.services;

import com.kousenit.shopping.dto.ProductRequest;
import com.kousenit.shopping.dto.ProductResponse;
import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.exceptions.InsufficientStockException;
import com.kousenit.shopping.exceptions.ProductNotFoundException;
import com.kousenit.shopping.exceptions.ProductValidationException;
import com.kousenit.shopping.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductResponse.from(product);
    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination: {}", pageable);
        return productRepository.findAll(pageable)
                .map(ProductResponse::from);
    }

    public List<ProductResponse> searchProductsByName(String name) {
        log.info("Searching products by name: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public List<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Fetching products in price range: {} - {}", minPrice, maxPrice);
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Min price cannot be greater than max price");
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product with SKU: {}", request.sku());

        validateProductRequest(request);

        Product product = new Product();
        product.setName(request.name());
        product.setPrice(request.price());
        product.setDescription(request.description());
        product.setQuantity(request.quantity());
        product.setSku(request.sku());
        product.setContactEmail(request.contactEmail());

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        return ProductResponse.from(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        validateProductRequest(request);

        // Check if SKU is being changed and if new SKU already exists
        if (!product.getSku().equals(request.sku()) &&
                productRepository.existsBySku(request.sku())) {
            throw new ProductValidationException("sku", request.sku(),
                    "Product with SKU " + request.sku() + " already exists");
        }

        product.setName(request.name());
        product.setPrice(request.price());
        product.setDescription(request.description());
        product.setQuantity(request.quantity());
        product.setSku(request.sku());
        product.setContactEmail(request.contactEmail());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully");
        return ProductResponse.from(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted successfully");
    }

    @Transactional
    public ProductResponse updateStock(Long id, Integer newQuantity) {
        log.info("Updating stock for product {}: new quantity {}", id, newQuantity);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        Integer oldQuantity = product.getQuantity();
        product.setQuantity(newQuantity);
        Product updatedProduct = productRepository.save(product);

        log.info("Stock updated for product {}: {} -> {}", id, oldQuantity, newQuantity);
        return ProductResponse.from(updatedProduct);
    }

    @Transactional
    public ProductResponse reserveStock(Long id, Integer quantity) {
        log.info("Reserving {} units of product {}", quantity, id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (!product.hasStock(quantity)) {
            throw new InsufficientStockException(id, quantity, product.getQuantity());
        }

        product.decrementStock(quantity);
        Product updatedProduct = productRepository.save(product);

        log.info("Reserved {} units of product {}. Remaining stock: {}",
                quantity, id, updatedProduct.getQuantity());
        return ProductResponse.from(updatedProduct);
    }

    @Transactional
    public ProductResponse addStock(Long id, Integer quantity) {
        log.info("Adding {} units to product {}", quantity, id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }

        product.incrementStock(quantity);
        Product updatedProduct = productRepository.save(product);

        log.info("Added {} units to product {}. New stock: {}",
                quantity, id, updatedProduct.getQuantity());
        return ProductResponse.from(updatedProduct);
    }

    public List<ProductResponse> getLowStockProducts(Integer threshold) {
        log.info("Fetching products with stock below {}", threshold);
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public List<ProductResponse> getExpensiveProducts(BigDecimal minPrice) {
        log.info("Fetching products with price above {}", minPrice);
        return productRepository.findExpensiveProducts(minPrice)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public long count() {
        return productRepository.count();
    }

    private void validateProductRequest(ProductRequest request) {
        // Additional business validation beyond bean validation
        if (request.price() != null && request.price().scale() > 2) {
            throw new ProductValidationException("price", request.price(),
                    "Price can have at most 2 decimal places");
        }
    }
}
```

**Key Features:**
- **Class-Level @Transactional(readOnly = true)** - Default for read operations
- **Method-Level @Transactional** - Write operations explicitly marked
- **Returns DTOs** - Never expose entities directly to controllers
- **Lombok Annotations** - @RequiredArgsConstructor for constructor injection, @Slf4j for logging
- **No Interfaces** - Simpler, more maintainable for most applications

[Back to Table of Contents](#table-of-contents)

---

## Lab 6: Create REST Controller with Full CRUD Operations

**Objective**: Implement a RESTful controller with proper HTTP semantics and status codes.

### Step 6.1: Create ProductRestController

Create `src/main/java/com/kousenit/shopping/controllers/ProductRestController.java`:

```java
package com.kousenit.shopping.controllers;

import com.kousenit.shopping.dto.ProductRequest;
import com.kousenit.shopping.dto.ProductResponse;
import com.kousenit.shopping.dto.StockUpdateRequest;
import com.kousenit.shopping.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@SuppressWarnings("LoggingSimilarMessage")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductRestController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        log.info("GET /api/v1/products/{}", id);
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/products - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String name) {
        log.info("GET /api/v1/products/search?name={}", name);
        List<ProductResponse> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ProductResponse>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        log.info("GET /api/v1/products/price-range?minPrice={}&maxPrice={}", minPrice, maxPrice);
        List<ProductResponse> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("POST /api/v1/products - Creating product with SKU: {}", request.sku());
        ProductResponse product = productService.createProduct(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(product.id())
            .toUri();

        return ResponseEntity.created(location).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        log.info("PUT /api/v1/products/{} - Updating product", id);
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/v1/products/{}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        log.info("PUT /api/v1/products/{}/stock - New quantity: {}", id, request.quantity());
        ProductResponse product = productService.updateStock(id, request.quantity());
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{id}/reserve-stock")
    public ResponseEntity<ProductResponse> reserveStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        log.info("POST /api/v1/products/{}/reserve-stock - Quantity: {}", id, request.quantity());
        ProductResponse product = productService.reserveStock(id, request.quantity());
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{id}/add-stock")
    public ResponseEntity<ProductResponse> addStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        log.info("POST /api/v1/products/{}/add-stock - Quantity: {}", id, request.quantity());
        ProductResponse product = productService.addStock(id, request.quantity());
        return ResponseEntity.ok(product);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        log.info("GET /api/v1/products/low-stock?threshold={}", threshold);
        List<ProductResponse> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/expensive")
    public ResponseEntity<List<ProductResponse>> getExpensiveProducts(
            @RequestParam(defaultValue = "100.00") BigDecimal minPrice) {
        log.info("GET /api/v1/products/expensive?minPrice={}", minPrice);
        List<ProductResponse> products = productService.getExpensiveProducts(minPrice);
        return ResponseEntity.ok(products);
    }
}
```

**Key Features:**
- **Proper HTTP Status Codes** - 200 OK, 201 Created, 204 No Content
- **Location Header** - Created resources return their URI
- **Pagination Support** - @PageableDefault for sensible defaults
- **@Valid** - Bean Validation triggers automatically
- **ResponseEntity** - Full control over HTTP responses

[Back to Table of Contents](#table-of-contents)

---

## Lab 7: Add Global Exception Handling with RFC 7807 ProblemDetail

**Objective**: Implement centralized exception handling using Spring's RFC 7807 ProblemDetail support for standardized error responses.

### Step 7.1: Create GlobalExceptionHandler

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
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String VALIDATION_FAILED = "Validation failed";

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        logger.warn("Validation failed for request: {}", ex.getMessage());

        List<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());

        ex.getBindingResult().getGlobalErrors().forEach(error ->
                validationErrors.add(ValidationError.of(
                        error.getObjectName(),
                        null,
                        error.getDefaultMessage(),
                        error.getCode()
                )));

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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        logger.error("Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity violation. This operation conflicts with existing data constraints.";
        String type = "https://api.shopping.com/problems/data-integrity";
        String title = "Data Integrity Violation";

        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Unique index or primary key violation")) {
                if (ex.getMessage().contains("IDX_PRODUCT_SKU") || ex.getMessage().contains("sku")) {
                    message = "A product with this SKU already exists.";
                    type = "https://api.shopping.com/problems/duplicate-sku";
                    title = "Duplicate SKU";
                } else {
                    message = "A record with this information already exists.";
                }
            } else if (ex.getMessage().contains("constraint")) {
                message = "This operation violates a data constraint.";
            }
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, message);
        problemDetail.setType(URI.create(type));
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

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

        assert ex.getRequiredType() != null;
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

        if (logger.isDebugEnabled()) {
            problemDetail.setProperty("debugMessage", ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

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

**Key Features:**
- **RFC 7807 ProblemDetail** - Standardized error format
- **Specific Exception Handlers** - Different status codes per exception type
- **Rich Error Details** - Includes timestamps, URIs, and context
- **Duplicate SKU Detection** - Special handling for database constraint violations

[Back to Table of Contents](#table-of-contents)

---

## Lab 8: Database Initialization with CommandLineRunner

**Objective**: Populate the database with sample data on application startup.

### Step 8.1: Create AppConfig

Create `src/main/java/com/kousenit/shopping/config/AppConfig.java`:

```java
package com.kousenit.shopping.config;

import com.kousenit.shopping.dto.ProductRequest;
import com.kousenit.shopping.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppConfig {

    private final ProductService productService;

    @Bean
    @Profile("!test")
    public CommandLineRunner initDatabase() {
        return args -> {
            log.info("Initializing database with sample products...");

            if (productService.count() > 0) {
                log.info("Database already contains {} products. Skipping initialization.",
                        productService.count());
                return;
            }

            List<ProductRequest> products = List.of(
                    createProduct("MacBook Pro 16\"", new BigDecimal("2499.99"),
                            "High-performance laptop for professionals", 15, "APP-000001", "sales@tech.com"),

                    createProduct("iPhone 15 Pro", new BigDecimal("999.99"),
                            "Latest flagship smartphone with advanced camera system", 50, "APP-000002",
                            "sales@tech.com"),

                    createProduct("AirPods Pro", new BigDecimal("249.99"),
                            "Premium wireless earbuds with active noise cancellation", 100, "APP-000003",
                            "sales@tech.com"),

                    createProduct("iPad Air", new BigDecimal("599.99"),
                            "Versatile tablet for work and play", 30, "APP-000004", "sales@tech.com"),

                    createProduct("Apple Watch Series 9", new BigDecimal("399.99"),
                            "Advanced health and fitness tracking smartwatch", 25, "APP-000005", "sales@tech.com"),

                    createProduct("Magic Keyboard", new BigDecimal("299.99"),
                            "Wireless keyboard with Touch ID", 40, "APP-000006", "accessories@tech.com"),

                    createProduct("Studio Display", new BigDecimal("1599.99"),
                            "27-inch 5K Retina display", 8, "APP-000007", "displays@tech.com"),

                    createProduct("Mac Mini", new BigDecimal("599.99"),
                            "Compact desktop computer with M2 chip", 20, "APP-000008", "sales@tech.com"),

                    createProduct("HomePod mini", new BigDecimal("99.99"),
                            "Compact smart speaker with amazing sound", 60, "APP-000009", "audio@tech.com"),

                    createProduct("Apple TV 4K", new BigDecimal("179.99"),
                            "Stream and watch in brilliant 4K HDR", 35, "APP-000010", "entertainment@tech.com"),

                    createProduct("USB-C Cable", new BigDecimal("19.99"),
                            "2-meter charging cable", 200, "ACC-000001", "accessories@tech.com"),

                    createProduct("MagSafe Charger", new BigDecimal("39.99"),
                            "Wireless charging made simple", 150, "ACC-000002", "accessories@tech.com"),

                    createProduct("Leather Case", new BigDecimal("59.99"),
                            "Premium leather case for iPhone", 80, "ACC-000003", "accessories@tech.com"),

                    createProduct("Screen Protector", new BigDecimal("9.99"),
                            "Tempered glass screen protection", 300, "ACC-000004", "accessories@tech.com"),

                    createProduct("External SSD 1TB", new BigDecimal("149.99"),
                            "High-speed portable storage", 5, "STG-000001", "storage@tech.com"));

            products.forEach(productService::createProduct);
            log.info("Database initialized with {} products", products.size());
        };
    }

    private ProductRequest createProduct(String name, BigDecimal price, String description,
            int quantity, String sku, String email) {
        return new ProductRequest(name, price, description, quantity, sku, email);
    }
}
```

**Key Features:**
- **@Profile("!test")** - CommandLineRunner doesn't run during tests
- **Check Before Insert** - Only initializes if database is empty
- **Uses Service Layer** - Goes through proper business logic
- **Clean Helper Method** - DRY principle for product creation

[Back to Table of Contents](#table-of-contents)

---

## Lab 9: Configure Application with Production-Ready Settings

**Objective**: Create comprehensive configuration with profiles, logging, and monitoring.

### Step 9.1: Create application.yml

Create `src/main/resources/application.yml`:

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

**Key Features:**
- **HikariCP Configuration** - Production-ready connection pooling
- **ProblemDetail Enabled** - RFC 7807 support
- **Comprehensive Logging** - Different levels for different packages
- **Test Profile** - Separate database and reduced logging
- **Actuator Endpoints** - Health checks and monitoring
- **YAML `---` Separator** - Clean profile separation

[Back to Table of Contents](#table-of-contents)

---

## Lab 10: Write Comprehensive Tests

**Objective**: Create integration tests using @DirtiesContext for proper test isolation.

### Step 10.1: Create Integration Tests

Create `src/test/java/com/kousenit/shopping/ShoppingApplicationIntegrationTest.java`:

```java
package com.kousenit.shopping;

import tools.jackson.databind.ObjectMapper;
import com.kousenit.shopping.dto.ProductRequest;
import com.kousenit.shopping.dto.ProductResponse;
import com.kousenit.shopping.dto.StockUpdateRequest;
import com.kousenit.shopping.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
            "INT-123456",
            "updated@example.com"
        );

        mockMvc.perform(put("/api/v1/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Integration Product"))
            .andExpect(jsonPath("$.price").value(249.99))
            .andExpect(jsonPath("$.quantity").value(30));

        // Step 4: Reserve stock
        StockUpdateRequest reserveRequest = new StockUpdateRequest(10);

        mockMvc.perform(post("/api/v1/products/" + productId + "/reserve-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserveRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(20));

        // Step 5: Delete the product
        mockMvc.perform(delete("/api/v1/products/" + productId))
            .andExpect(status().isNoContent());

        // Verify deletion
        assertThat(productRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle duplicate SKU with HTTP 409 Conflict")
    void testDuplicateSkuHandling() throws Exception {
        // Create first product
        ProductRequest firstProduct = new ProductRequest(
            "First Product",
            new BigDecimal("99.99"),
            "First product description",
            10,
            "DUP-123456",
            "first@example.com"
        );

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstProduct)))
            .andExpect(status().isCreated());

        // Try to create second product with same SKU
        ProductRequest duplicateProduct = new ProductRequest(
            "Second Product",
            new BigDecimal("149.99"),
            "Second product description",
            5,
            "DUP-123456", // Same SKU
            "second@example.com"
        );

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateProduct)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.title").value("Duplicate SKU"));
    }

    @Test
    @DisplayName("Should handle validation errors with detailed response")
    void testValidationErrors() throws Exception {
        ProductRequest invalidProduct = new ProductRequest(
            "AB", // Too short
            new BigDecimal("0.00"), // Too low
            null,
            -5, // Negative
            "INVALID", // Wrong format
            "not-an-email" // Invalid email
        );

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors").isArray())
            .andExpect(jsonPath("$.validationErrors.length()").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Should handle insufficient stock with detailed error")
    void testInsufficientStockError() throws Exception {
        // Create product with limited stock
        ProductRequest product = new ProductRequest(
            "Low Stock Product",
            new BigDecimal("99.99"),
            "Product with low stock",
            5,
            "LOW-123456",
            "stock@example.com"
        );

        String response = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ProductResponse created = objectMapper.readValue(response, ProductResponse.class);

        // Try to reserve more than available
        StockUpdateRequest excessiveReservation = new StockUpdateRequest(10);

        mockMvc.perform(post("/api/v1/products/" + created.id() + "/reserve-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(excessiveReservation)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Insufficient Stock"))
            .andExpect(jsonPath("$.productId").value(created.id()))
            .andExpect(jsonPath("$.requestedQuantity").value(10))
            .andExpect(jsonPath("$.availableQuantity").value(5));
    }
}
```

### Step 10.2: Create Service Tests

Create `src/test/java/com/kousenit/shopping/services/ProductServiceTest.java`:

```java
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
                "test@example.com");
    }

    @Test
    @DisplayName("Should get product by id successfully")
    void testGetProductByIdSuccess() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        ProductResponse result = productService.getProductById(1L);

        // Then
        assertThat(result)
                .isNotNull()
                .returns(1L, ProductResponse::id)
                .returns("Test Product", ProductResponse::name)
                .returns(new BigDecimal("99.99"), ProductResponse::price);
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
    @DisplayName("Should throw exception when insufficient stock")
    void testReserveStockInsufficientStock() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When/Then
        assertThrows(InsufficientStockException.class,
                () -> productService.reserveStock(1L, 50)); // More than available
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
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
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Test Product");
        verify(productRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProductSuccess() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

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
}
```

### Step 10.3: Run All Tests

```bash
# Run all tests
./gradlew test

# Run with coverage (if configured)
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests ShoppingApplicationIntegrationTest
./gradlew test --tests ProductServiceTest
```

**Key Testing Patterns:**
- **@DirtiesContext** - Ensures complete test isolation
- **@BeforeEach** - Clean database state for each test
- **@MockitoBean** - Modern Spring Boot 3.4+ annotation (replaces @MockBean)
- **@DisplayName** - Readable test descriptions
- **AssertJ** - Fluent assertions for better readability
- **Given-When-Then** - Clear test structure

[Back to Table of Contents](#table-of-contents)

---

## Running the Application

```bash
# Run the application
./gradlew bootRun

# Run all tests
./gradlew test

# Build the application
./gradlew build

# Clean build
./gradlew clean build
```

## Testing the API

### Using the H2 Console

Access the H2 database console at http://localhost:8080/h2-console

- **JDBC URL**: `jdbc:h2:mem:shopping`
- **Username**: `sa`
- **Password**: (empty)

You can now manually insert products without specifying timestamps:

```sql
-- Simple insert - timestamps auto-populated by database defaults
INSERT INTO products (name, price, description, quantity, sku, contact_email)
VALUES ('Manual Product', 79.99, 'Added via SQL', 15, 'MAN-123456', 'manual@example.com');

-- Or include timestamps explicitly if you prefer
INSERT INTO products (name, price, description, quantity, sku, contact_email, created_at, updated_at)
VALUES ('Manual Product 2', 89.99, 'Another SQL insert', 20, 'MAN-123457', 'manual2@example.com',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

### Using cURL

```bash
# Get all products (paginated)
curl http://localhost:8080/api/v1/products

# Get product by ID
curl http://localhost:8080/api/v1/products/1

# Search products by name
curl "http://localhost:8080/api/v1/products/search?name=iPhone"

# Get products in price range
curl "http://localhost:8080/api/v1/products/price-range?minPrice=100&maxPrice=500"

# Get low stock products
curl "http://localhost:8080/api/v1/products/low-stock?threshold=10"

# Create a new product
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "price": 99.99,
    "description": "A test product",
    "quantity": 10,
    "sku": "TST-123456",
    "contactEmail": "test@example.com"
  }'

# Update product
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Product",
    "price": 149.99,
    "description": "Updated description",
    "quantity": 20,
    "sku": "UPD-123456",
    "contactEmail": "updated@example.com"
  }'

# Update stock
curl -X PUT http://localhost:8080/api/v1/products/1/stock \
  -H "Content-Type: application/json" \
  -d '{"quantity": 50}'

# Reserve stock
curl -X POST http://localhost:8080/api/v1/products/1/reserve-stock \
  -H "Content-Type: application/json" \
  -d '{"quantity": 5}'

# Delete product
curl -X DELETE http://localhost:8080/api/v1/products/1
```

## Key Learning Points

### Modern Spring Boot Patterns
- **Records for DTOs** - Immutable, concise API contracts
- **No Service Interfaces** - Simpler for most applications
- **Lombok Integration** - Clean, readable code
- **@MockitoBean** - Modern Spring Boot 3.4+ testing

### Architecture Best Practices
- **DTO Pattern** - Decouple API from domain model
- **Service Layer** - Business logic encapsulation
- **RFC 7807 ProblemDetail** - Standardized errors
- **Transaction Management** - Proper @Transactional usage

### Testing Patterns
- **@DirtiesContext** - Complete test isolation
- **Integration Tests** - Test real database constraints
- **MockMvc** - Controller testing without HTTP
- **AssertJ** - Fluent, readable assertions

### Production Readiness
- **YAML Profiles** - Environment-specific configuration
- **HikariCP** - Connection pool tuning
- **Comprehensive Logging** - Debug production issues
- **Actuator Endpoints** - Health checks and monitoring

---

## What You've Built

Congratulations! You've created a production-ready Spring Boot application with:

✅ **Progressive Entity Design** - Basic JPA (Lab 1) → Bean Validation (Lab 4)
✅ **Clean Repository Layer** with custom queries
✅ **DTO Pattern** separating API from domain with independent validation
✅ **Custom Exceptions** for business logic errors
✅ **Service Layer** with proper transactions
✅ **RESTful API** with correct HTTP semantics
✅ **RFC 7807 Error Handling** with ProblemDetail
✅ **Database Initialization** with CommandLineRunner
✅ **Production Configuration** with profiles
✅ **Comprehensive Tests** with proper isolation

This application demonstrates enterprise-level patterns used in real-world production systems, learned progressively from basic concepts to advanced patterns.
