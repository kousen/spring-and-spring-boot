# CLAUDE.md - Spring Boot Labs

This file contains common commands and patterns for working with the Spring Boot lab projects. Use these commands when working with Claude Code or any development environment.

## Project Information

- **Spring Boot Version**: 3.5.3
- **Java Version**: 17+ (required)
- **Gradle Version**: 8.14.2
- **Package Structure**: `com.kousenit.*`

## Project Structure

```
spring-and-spring-boot/
├── demo/              # Basic Spring Boot web application
├── restclient/        # External API integration examples
├── persistence/       # Database access patterns
├── shopping/          # Enterprise shopping application (from advanced labs)
├── labs.md            # Comprehensive basic lab instructions
├── advanced-labs.md   # Enterprise development labs (shopping app)
└── README.md          # Project overview and quick start
```

## Common Commands

### Build Commands

```bash
# Build all projects from root
./gradlew build

# Build specific projects
cd demo && ./gradlew build
cd restclient && ./gradlew build  
cd persistence && ./gradlew build
cd shopping && ./gradlew build

# Clean and rebuild
./gradlew clean build

# Check for dependency updates
./gradlew dependencyUpdates
```

### Test Commands

```bash
# Run all tests
./gradlew test

# Run tests for specific project
cd demo && ./gradlew test
cd restclient && ./gradlew test
cd persistence && ./gradlew test
cd shopping && ./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run specific test class
./gradlew test --tests HelloControllerTest

# Run tests matching pattern
./gradlew test --tests "*Json*"
```

### Application Commands

```bash
# Run demo application (port 8080)
cd demo && ./gradlew bootRun

# Run restclient examples
cd restclient && ./gradlew bootRun

# Run persistence application with H2 console
cd persistence && ./gradlew bootRun

# Run shopping application (enterprise example)
cd shopping && ./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
./gradlew bootRun --args='--spring.profiles.active=test'
./gradlew bootRun --args='--spring.profiles.active=prod'

# Run with multiple profiles
./gradlew bootRun --args='--spring.profiles.active=dev,debug'

# Run with JVM options
./gradlew bootRun --args='--server.port=8081'

# Run with environment variables
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

### Development Commands

```bash
# Generate IDE files (IntelliJ)
./gradlew idea

# Generate IDE files (Eclipse)
./gradlew eclipse

# Check code style
./gradlew checkstyleMain checkstyleTest

# Create JAR files
./gradlew bootJar

# List all tasks
./gradlew tasks
```

## Testing Patterns

### Unit Testing with JUnit 5

```java
@Test
void shouldReturnGreeting() {
    // Given
    String name = "World";
    
    // When
    String result = service.greet(name);
    
    // Then
    assertEquals("Hello, World!", result);
}
```

### Integration Testing

```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ApplicationIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldReturnOkStatus() {
        ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

### MockMVC Testing

```java
@WebMvcTest(HelloController.class)
class HelloControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldReturnHelloWorld() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello, World!")));
    }
}
```

### Reactive Testing

```java
@Test
void shouldReturnUsersReactively() {
    service.getAllUsersAsync()
            .as(StepVerifier::create)
            .expectNextCount(10)
            .verifyComplete();
}
```

## Common Troubleshooting

### Build Issues

```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/

# Refresh dependencies
./gradlew build --refresh-dependencies

# Check Java version
java -version

# Check Gradle version
./gradlew --version
```

### Application Issues

```bash
# Check if port is in use
lsof -i :8080

# Kill process on port 8080
kill -9 $(lsof -ti:8080)

# Run with different port
./gradlew bootRun --args='--server.port=8081'
```

### Database Issues (H2)

```bash
# Access H2 Console
# URL: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:testdb
# Username: sa
# Password: (empty)

# Reset database
./gradlew clean bootRun
```

## API Testing

### Shopping API Endpoints (When running shopping application)

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
       "name": "New Product",
       "price": 99.99,
       "description": "A new product",
       "quantity": 10,
       "sku": "NEW-123456",
       "contactEmail": "contact@example.com"
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

### JSON Placeholder API (No Auth Required)

```bash
# GET requests
curl https://jsonplaceholder.typicode.com/users
curl https://jsonplaceholder.typicode.com/users/1
curl https://jsonplaceholder.typicode.com/users/1/posts

# POST requests
curl -X POST https://jsonplaceholder.typicode.com/posts \
     -H "Content-Type: application/json" \
     -d '{"userId": 1, "title": "Test Post", "body": "This is a test post"}'

curl -X POST https://jsonplaceholder.typicode.com/users \
     -H "Content-Type: application/json" \
     -d '{"name": "John Doe", "email": "john@example.com"}'

# DELETE requests
curl -X DELETE https://jsonplaceholder.typicode.com/posts/1
curl -X DELETE https://jsonplaceholder.typicode.com/users/1
```

### NASA Open Data API

```bash
# Test astronauts in space
curl http://api.open-notify.org/astros.json
```

## Modern Java Features Examples

### Records (Java 17+)

```java
public record User(
    Long id,
    String name,
    String email,
    Address address
) {}
```

### Text Blocks (Java 17+)

```java
String sql = """
    SELECT id, name, email 
    FROM users 
    WHERE active = true
    ORDER BY name
    """;
```

### RestClient (Spring Boot 3.2+)

```java
List<User> users = restClient.get()
    .uri("/users")
    .retrieve()
    .body(new ParameterizedTypeReference<List<User>>() {});
```

### JdbcClient (Spring Boot 3.2+)

```java
Optional<User> user = jdbcClient
    .sql("SELECT * FROM users WHERE id = :id")
    .param("id", userId)
    .query(User.class)
    .optional();
```

## Useful Development URLs

When applications are running:

- **Demo App**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
- **Actuator Health**: http://localhost:8080/actuator/health
- **Actuator Info**: http://localhost:8080/actuator/info

## IDE Configuration

### IntelliJ IDEA

1. Import as Gradle project
2. Enable annotation processing
3. Set Project SDK to Java 17+
4. Enable Spring Boot features

### VS Code

Required extensions:
- Extension Pack for Java
- Spring Boot Extension Pack
- Gradle for Java

## Performance Tips

```bash
# Use Gradle daemon (faster builds)
echo "org.gradle.daemon=true" >> ~/.gradle/gradle.properties

# Use parallel builds
echo "org.gradle.parallel=true" >> ~/.gradle/gradle.properties

# Increase memory for builds
echo "org.gradle.jvmargs=-Xmx2g -XX:MaxPermSize=512m" >> ~/.gradle/gradle.properties
```

## Security Notes

- Never commit API keys or secrets
- Use environment variables for configuration
- Keep dependencies updated
- Review security advisories regularly