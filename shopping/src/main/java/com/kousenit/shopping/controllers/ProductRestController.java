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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

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
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
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