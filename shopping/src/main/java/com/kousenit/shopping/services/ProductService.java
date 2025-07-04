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
    
    private void validateProductRequest(ProductRequest request) {
        // Additional business validation beyond bean validation
        if (request.price() != null && request.price().scale() > 2) {
            throw new ProductValidationException("price", request.price(), 
                "Price can have at most 2 decimal places");
        }
    }
}