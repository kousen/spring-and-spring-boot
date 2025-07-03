# Spring Boot Labs - Future Enhancements

This file tracks potential additions to the Spring Boot labs based on commonly used features in production applications.

## High Priority Topics

### 1. Spring Security ⭐⭐⭐⭐⭐
**Why:** Used in virtually every production Spring Boot application
**Complexity:** Medium
**Suggested Implementation:** Add to `demo` project

**Features to cover:**
- [ ] Basic authentication & authorization
- [ ] Username/password login with form-based auth
- [ ] Method-level security (`@PreAuthorize`, `@Secured`)
- [ ] JWT tokens for stateless authentication
- [ ] Password encoding (BCrypt)
- [ ] User management and roles
- [ ] CORS configuration for REST APIs
- [ ] Security testing with `@WithMockUser`

**Exercise Ideas:**
- Secure existing REST endpoints
- Create login/logout functionality
- Add user registration
- Implement role-based access control

### 2. Spring Boot Actuator ⭐⭐⭐⭐⭐
**Why:** Essential for production monitoring and operations
**Complexity:** Low-Medium
**Suggested Implementation:** Add to `persistence` project

**Features to cover:**
- [ ] Health checks and custom health indicators
- [ ] Application metrics and monitoring
- [ ] Info endpoints for deployment information
- [ ] Environment and configuration exposure
- [ ] Custom endpoints
- [ ] Security for actuator endpoints
- [ ] Integration with monitoring tools (Micrometer)

**Exercise Ideas:**
- Custom database health check
- Application info with Git commit details
- Custom metrics for business logic
- Monitoring database connection pools

### 3. Bean Validation (JSR-303/380) ⭐⭐⭐⭐
**Why:** Very common in web applications, proper input validation
**Complexity:** Low-Medium
**Suggested Implementation:** Enhance existing REST controllers

**Features to cover:**
- [ ] `@Valid` with request bodies
- [ ] Standard annotations (`@NotNull`, `@Size`, `@Email`, `@Pattern`)
- [ ] Custom validators and annotations
- [ ] Validation groups for different scenarios
- [ ] Error message customization
- [ ] Integration with `@ControllerAdvice`

**Exercise Ideas:**
- Add validation to Officer entity
- Validate JSON Placeholder API requests
- Custom validation for business rules
- Proper error responses for validation failures

### 4. Global Exception Handling ⭐⭐⭐⭐
**Why:** Critical for proper REST API design and error handling
**Complexity:** Medium
**Suggested Implementation:** Enhance REST exercises

**Features to cover:**
- [ ] `@ControllerAdvice` for global exception handling
- [ ] Custom exception types
- [ ] Proper HTTP status codes
- [ ] Standardized error response format
- [ ] Exception handling for different layers (service, repository)
- [ ] Logging best practices

**Exercise Ideas:**
- Global error handler for REST APIs
- Custom business exceptions
- Database constraint violation handling
- API error response standardization

## Medium Priority Topics

### 5. Caching ⭐⭐⭐
**Why:** Common performance optimization
**Complexity:** Medium
**Suggested Implementation:** Add to `persistence` project

**Features to cover:**
- [ ] `@Cacheable`, `@CacheEvict`, `@CachePut`
- [ ] Cache configuration
- [ ] Different cache providers (Caffeine, Redis)
- [ ] Cache key generation strategies
- [ ] Cache statistics and monitoring

### 6. Async Processing & Scheduling ⭐⭐⭐
**Why:** Important for performance and background processing
**Complexity:** Medium
**Suggested Implementation:** New exercise or enhance existing

**Features to cover:**
- [ ] `@Async` methods and configuration
- [ ] `@Scheduled` tasks and cron expressions
- [ ] CompletableFuture patterns
- [ ] Thread pool configuration
- [ ] Async testing strategies

### 7. Application Events ⭐⭐
**Why:** Useful for decoupled component communication
**Complexity:** Low-Medium
**Suggested Implementation:** Add to existing exercises

**Features to cover:**
- [ ] `@EventListener` annotation
- [ ] Custom application events
- [ ] Async event processing
- [ ] Event ordering and conditional listeners

### 8. Testing Enhancements ⭐⭐⭐
**Why:** More comprehensive testing strategies
**Complexity:** Medium
**Suggested Implementation:** Enhance existing tests

**Features to cover:**
- [ ] `@MockBean` and `@SpyBean`
- [ ] Test slices (`@WebMvcTest`, `@DataJpaTest`, `@JsonTest`)
- [ ] Integration testing strategies
- [ ] Performance testing basics
- [ ] Contract testing with Spring Cloud Contract

## Lower Priority / Advanced Topics

### 9. Messaging & Queues ⭐⭐
**Why:** Common in distributed systems
**Complexity:** High (requires external infrastructure)

**Features:**
- [ ] Spring Boot with RabbitMQ
- [ ] Apache Kafka integration
- [ ] Message-driven architecture patterns

### 10. WebSocket Communication ⭐⭐
**Why:** Real-time communication needs
**Complexity:** Medium-High

**Features:**
- [ ] WebSocket configuration
- [ ] STOMP messaging
- [ ] Real-time notifications

### 11. Spring Cloud Basics ⭐
**Why:** Microservices architecture
**Complexity:** High (requires multiple services)

**Features:**
- [ ] Service discovery
- [ ] Configuration server
- [ ] Circuit breakers

## Implementation Strategy

### Phase 1: Essential Production Features
1. **Spring Security** - Basic authentication and authorization
2. **Spring Boot Actuator** - Health checks and monitoring
3. **Bean Validation** - Input validation for REST APIs

### Phase 2: Enhanced Functionality
4. **Exception Handling** - Global error handling
5. **Caching** - Performance optimization
6. **Testing Enhancements** - More comprehensive test strategies

### Phase 3: Advanced Features
7. **Async Processing** - Background tasks and scheduling
8. **Application Events** - Decoupled communication
9. **Messaging** - Queue-based communication

## Notes

- Each new topic should integrate with existing exercises rather than creating entirely new projects
- Maintain the current pattern of progressive complexity
- Include comprehensive tests for each new feature
- Focus on practical, real-world usage patterns
- Keep Docker/external dependencies optional where possible
- Update CLAUDE.md with new commands and troubleshooting for each addition

## Student Feedback Integration

Track commonly asked questions and requests:
- [ ] "How do I secure my REST APIs?" → Spring Security
- [ ] "How do I monitor my application in production?" → Actuator
- [ ] "How do I validate user input?" → Bean Validation
- [ ] "How do I handle errors properly?" → Exception Handling
- [ ] "How do I make my app faster?" → Caching
- [ ] "How do I run background tasks?" → Async/Scheduling