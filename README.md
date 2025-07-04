# Spring and Spring Boot Labs

Comprehensive hands-on exercises for learning Spring Boot fundamentals, from basic web applications to database persistence patterns.

## Prerequisites

- **Java 17 or later** (Spring Boot 3.x requires Java 17+)
- **Spring Boot 3.5.3** (current version)
- **Gradle 8.14.2** (latest stable)
- IDE with Spring Boot support (IntelliJ IDEA, Spring Tool Suite, or VS Code)

## Lab Materials

### 📚 **labs.md** - Comprehensive Basic Labs
Foundation labs covering Spring Boot fundamentals from web development to database persistence.

### 🚀 **advanced-labs.md** - Enterprise Development Labs  
Advanced labs building a complete shopping application with enterprise patterns. **Updated to match the final implementation exactly** - students can copy from the solution when needed:
- Progressive entity design with JPA validation and Lombok
- Spring Data repositories with custom queries and indexes
- REST API development with modern DTOs and full CRUD operations
- Global exception handling with RFC 7807 ProblemDetail
- Transaction management and comprehensive testing strategies
- YAML configuration with profile separation using `---`

## Project Structure

This repository contains three independent Spring Boot projects that demonstrate different aspects of the framework:

### 📁 **demo** - Basic Spring Boot Web Application
- **Package**: `com.kousenit.demo`
- **Purpose**: Introduction to Spring Boot web development
- **Features**:
  - Thymeleaf templating
  - REST controllers with JSON responses
  - Unit and integration testing
  - Basic web forms and static content

### 📁 **restclient** - External API Integration
- **Package**: `com.kousenit.restclient`
- **Purpose**: Consuming external REST APIs
- **Features**:
  - Modern `RestClient` for synchronous calls
  - `WebClient` for reactive/asynchronous calls
  - JSON Placeholder API integration (no API keys required)
  - NASA Open Data API examples
  - Comprehensive error handling

### 📁 **persistence** - Database Access Patterns
- **Package**: `com.kousenit.persistence`
- **Purpose**: Various approaches to data persistence
- **Features**:
  - Modern `JdbcClient` (Spring Boot 3.2+)
  - Traditional `JdbcTemplate`
  - JPA with Hibernate
  - Spring Data JPA repositories
  - H2 in-memory database

### 📁 **shopping** - Enterprise Shopping Application (NEW)
- **Package**: `com.kousenit.shopping`
- **Purpose**: Complete enterprise application demonstrating advanced patterns
- **Features**:
  - JPA entities with comprehensive validation and Lombok
  - Database indexes for performance optimization
  - Spring Data JPA with custom queries and sophisticated repository patterns
  - Service layer with transaction management and business logic
  - REST API with modern DTOs and full CRUD operations
  - Global exception handling with RFC 7807 ProblemDetail responses
  - YAML configuration with test profile isolation
  - Sample data initialization with profile-aware CommandLineRunner
  - Comprehensive test suite with modern @MockitoBean annotations
  - Production-ready logging and monitoring endpoints

## Quick Start

### Build All Projects
```bash
# From the root directory
./gradlew build

# Or build individual projects
cd demo && ./gradlew build
cd restclient && ./gradlew build
cd persistence && ./gradlew build
cd shopping && ./gradlew build
```

### Run Applications
```bash
# Demo application (port 8080)
cd demo && ./gradlew bootRun

# REST client examples
cd restclient && ./gradlew bootRun

# Persistence examples with H2 console
cd persistence && ./gradlew bootRun

# Shopping application (port 8080)
cd shopping && ./gradlew bootRun
```

### Run Tests
```bash
# All tests
./gradlew test

# Specific project tests
cd demo && ./gradlew test
cd restclient && ./gradlew test
cd persistence && ./gradlew test
cd shopping && ./gradlew test
```

## Key Learning Topics

### Modern Spring Boot Features
- **Spring Boot 3.5.3** with Java 17+ features
- **Records** for immutable data classes and DTOs
- **Text blocks** for readable SQL and JSON
- **RestClient** as the modern replacement for RestTemplate
- **JdbcClient** as the modern alternative to JdbcTemplate
- **@MockitoBean** replacing deprecated @MockBean
- **Lombok** for reducing boilerplate with proper IDE integration
- **YAML configuration** with `---` profile separation
- **RFC 7807 ProblemDetail** for standardized error responses

### Web Development
- RESTful web services with `@RestController`
- Server-side rendering with Thymeleaf
- Static content and form handling
- HTTP client integration patterns

### Data Access
- **Modern JDBC**: JdbcClient and JdbcTemplate patterns
- **JPA Entity Mapping**: Annotations and relationship management
- **Spring Data JPA**: Repository pattern with automatic query generation
- **Database Configuration**: Multi-environment setup with profiles
- **Connection Management**: H2 for development, PostgreSQL for production

### Configuration Management
- **Property Injection**: `@Value` annotation for external configuration
- **Profile-Specific Properties**: `application-{profile}.yml` files
- **Environment Variables**: Secure configuration for production
- **Feature Toggles**: Profile-based conditional bean creation

### Testing
- **Unit Testing**: JUnit 5 with comprehensive assertions and modern patterns
- **Integration Testing**: `@SpringBootTest` with profile activation and test isolation
- **Web Layer Testing**: `@WebMvcTest` with MockMVC for REST API testing
- **Service Layer Testing**: `@MockitoBean` for dependency mocking
- **Entity Validation Testing**: Bean validation with constraint violation testing
- **Reactive Testing**: `StepVerifier` for WebClient flows
- **Database Testing**: Repository testing with `@DataJpaTest`
- **Profile Testing**: `@ActiveProfiles` for environment-specific tests

## API Integrations

### JSON Placeholder (No API Key Required)
- **Users API**: Complex nested JSON structures with address and company data
- **Posts API**: Complete CRUD operations (GET, POST, DELETE)
- **Configuration**: Externalized URLs and timeouts with `@Value` annotation
- **Demonstrates**: Modern record classes, full HTTP method support, sync/async patterns, robust error handling

### NASA Open Data
- **Astronauts in Space**: Real-time space station crew data
- **Demonstrates**: WebClient usage, reactive programming

## Development Environment

### Recommended IDEs
- **IntelliJ IDEA Ultimate** - Full Spring Boot support
- **Spring Tool Suite** - Eclipse-based with Spring tools
- **VS Code** - With Spring Boot extensions

### Database Access

**Development Profile (default):**
- **H2 Console**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:shopping` (or project-specific name)
- **Username**: `sa`
- **Password**: (empty)

**Profile Switching:**
```bash
# Run with different profiles
./gradlew bootRun --args='--spring.profiles.active=dev'    # H2 (default)
./gradlew bootRun --args='--spring.profiles.active=test'   # H2 optimized for testing
./gradlew bootRun --args='--spring.profiles.active=prod'   # PostgreSQL via Testcontainers

# Shopping application with debug logging
cd shopping && ./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Shopping Application Endpoints:**
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health
- **API Documentation**: http://localhost:8080/api/v1/products

### Optional Requirements

**Docker Desktop (for advanced testing):**
- Required for PostgreSQL Testcontainers integration
- Production profile tests will be skipped if Docker is unavailable
- All core functionality works without Docker using H2 database

## Modern Java Features Used

- **Records** - Immutable data classes for JSON mapping
- **Text Blocks** - Readable multi-line SQL queries
- **Pattern Matching** - Enhanced switch expressions
- **Local Variable Type Inference** - `var` keyword usage
- **Optional** - Null-safe API responses

## Contributing

This project follows modern Spring Boot best practices:
- Package structure: `com.kousenit.*`
- Java 17+ features throughout
- Comprehensive test coverage
- Modern dependency management
- Clean separation of concerns

## License

See [LICENSE](LICENSE) file for details.
