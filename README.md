# Spring and Spring Boot Labs

Comprehensive hands-on exercises for learning Spring Boot fundamentals, from basic web applications to database persistence patterns.

## Prerequisites

- **Java 17 or later** (Spring Boot 3.x requires Java 17+)
- **Spring Boot 3.5.3** (current version)
- **Gradle 8.14.2** (latest stable)
- IDE with Spring Boot support (IntelliJ IDEA, Spring Tool Suite, or VS Code)

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

## Quick Start

### Build All Projects
```bash
# From the root directory
./gradlew build

# Or build individual projects
cd demo && ./gradlew build
cd restclient && ./gradlew build
cd persistence && ./gradlew build
```

### Run Applications
```bash
# Demo application (port 8080)
cd demo && ./gradlew bootRun

# REST client examples
cd restclient && ./gradlew bootRun

# Persistence examples with H2 console
cd persistence && ./gradlew bootRun
```

### Run Tests
```bash
# All tests
./gradlew test

# Specific project tests
cd demo && ./gradlew test
cd restclient && ./gradlew test
cd persistence && ./gradlew test
```

## Key Learning Topics

### Modern Spring Boot Features
- **Spring Boot 3.5.3** with Java 17+ features
- **Records** for immutable data classes
- **Text blocks** for readable SQL and JSON
- **RestClient** as the modern replacement for RestTemplate
- **JdbcClient** as the modern alternative to JdbcTemplate

### Web Development
- RESTful web services with `@RestController`
- Server-side rendering with Thymeleaf
- Static content and form handling
- HTTP client integration patterns

### Data Access
- JDBC with modern Spring abstractions
- JPA entity mapping and relationships
- Repository pattern with Spring Data
- Database migration and initialization

### Testing
- Unit testing with JUnit 5
- Integration testing with `@SpringBootTest`
- MockMVC for web layer testing
- Reactive testing with `StepVerifier`

## API Integrations

### JSON Placeholder (No API Key Required)
- **Users API**: Complex nested JSON structures
- **Posts API**: Simple CRUD operations
- **Demonstrates**: Modern record classes, error handling, both sync/async patterns

### NASA Open Data
- **Astronauts in Space**: Real-time space station crew data
- **Demonstrates**: WebClient usage, reactive programming

## Development Environment

### Recommended IDEs
- **IntelliJ IDEA Ultimate** - Full Spring Boot support
- **Spring Tool Suite** - Eclipse-based with Spring tools
- **VS Code** - With Spring Boot extensions

### Database Access
When running the persistence project, access the H2 console at:
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (empty)

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
