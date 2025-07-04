package com.kousenit.shopping.config;

import com.kousenit.shopping.entities.Product;
import com.kousenit.shopping.repositories.ProductRepository;
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
    
    private final ProductRepository productRepository;
    
    @Bean
    @Profile("!test")
    public CommandLineRunner initDatabase() {
        return args -> {
            log.info("Initializing database with sample products...");
            
            if (productRepository.count() > 0) {
                log.info("Database already contains {} products. Skipping initialization.", 
                        productRepository.count());
                return;
            }
            
            List<Product> products = List.of(
                createProduct("MacBook Pro 16\"", new BigDecimal("2499.99"), 
                    "High-performance laptop for professionals", 15, "APP-000001", "sales@tech.com"),
                
                createProduct("iPhone 15 Pro", new BigDecimal("999.99"), 
                    "Latest flagship smartphone with advanced camera system", 50, "APP-000002", "sales@tech.com"),
                
                createProduct("AirPods Pro", new BigDecimal("249.99"), 
                    "Premium wireless earbuds with active noise cancellation", 100, "APP-000003", "sales@tech.com"),
                
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
                    "High-speed portable storage", 5, "STG-000001", "storage@tech.com")
            );
            
            productRepository.saveAll(products);
            log.info("Database initialized with {} products", products.size());
        };
    }
    
    private Product createProduct(String name, BigDecimal price, String description, 
                                 int quantity, String sku, String email) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setQuantity(quantity);
        product.setSku(sku);
        product.setContactEmail(email);
        return product;
    }
}