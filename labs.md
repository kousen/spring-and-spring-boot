# Spring Boot Labs

This document contains hands-on exercises for learning Spring Boot fundamentals, from basic web applications to database persistence patterns.

## Prerequisites

- **Java 17 or later** (Spring Boot 3.x requires Java 17+)
- **Spring Boot 3.5.3** (current version)
- IDE with Spring Boot support (IntelliJ IDEA, Spring Tool Suite, or VS Code)

> [!IMPORTANT]
> These labs are designed for Spring Boot 3.5.3, which requires Java 17 or later. All code examples use modern Java features including records, text blocks, pattern matching, and enhanced switch expressions.

## Table of Contents

1. [Creating a New Project](#creating-a-new-project)
2. [Add a REST Controller](#add-a-rest-controller)
3. [Building a REST Client](#building-a-rest-client)
4. [HTTP Interfaces (Spring Boot 3+)](#http-interfaces-spring-boot-3)
5. [Consuming External APIs](#consuming-external-apis)
6. [Using the JDBC Template](#using-the-jdbc-template)
7. [Using the JDBC Client (Spring Boot 3.2+)](#using-the-jdbc-client-spring-boot-32)
8. [Using JPA entities and Spring Data JPA](#using-jpa-entities-and-spring-data-jpa)

## Creating a New Project

1. Go to http://start.spring.io to access the Spring Initializr
2. In the "Generate a" drop-down, switch from "Maven Project" to "Gradle Project"
3. Specify the Group as `com.kousenit` and the Artifact as `demo`
4. Add the _Spring Web_ and _Thymeleaf_ dependencies
5. Click the "Generate Project" button to download a zip file containing the project files
6. Unzip the downloaded "demo.zip" file into any directory you like (but remember where it is)
7. Import the project into your IDE
   - If you are using IntelliJ IDEA, import the project by selecting the "Import Project" link on the Welcome page and navigating to the `build.gradle` file inside the unzipped archive
   - If you are using Spring Tool Suite (or any other Eclipse-based tool) with Gradle support, you can import the project as an "Existing Gradle project" by navigating to the root of the project and accepting all the defaults.
   - If you don't have Gradle support in your Eclipse-based IDE, generate an Eclipse project using the included `gradlew` script.
   - First you need to add the `eclipse` plugin to the `build.gradle` file. Open that file in any text editor and type the following line inside the `plugins` block:

```java
plugins {
    // ... existing plugins ...

    id 'eclipse'
}
```

   - Now navigate to the project root directory in a command window and run the following command:

   > gradlew cleanEclipse eclipse

> **Note:** On a Unix-based machine (including Macs), use `./gradlew` for the command

   - Now you should be able to import the project into Eclipse as an existing Eclipse project (File -> Import... -> General -> Existing Projects Into Workspace)

8. As part of the import process, the IDE will download all the required dependencies
9. Open the file `src/main/java/com/kousenit/demo/DemoApplication.java` and note that it contains a standard Java "main" method (with signature: `public static void main(String[] args)`)
10. Start the application by running this method. There won't be any web components available yet, but you can see the start up of the application in the command window.
11. Add a controller by creating a file called `com.kousenit.demo.controllers.HelloController` in the `src/main/java` directory

> [!NOTE]
> The goal is to have the `HelloController` class in the `com.kousenit.demo.controllers` package starting at the root directory `src/main/java`

12. The code for the `HelloController` is:

```java
package com.kousenit.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HelloController {

    @GetMapping("/hello")
    public String sayHello(
            @RequestParam(value = "name", required = false,
                          defaultValue = "World") String name, Model model) {
        model.addAttribute("user", name);
        return "welcome";
    }
}
```

13. Create a file called `welcome.html` in the `src/main/resources/templates` folder
14. The code for the `welcome.html` file is:

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org" lang="en">
<head>
    <title>Hello, World!</title>
</head>
<body>
    <h2 th:text="'Hello, ' + ${user} + '!'"></h2>
</body>
</html>
```

15. Start up the application and navigate to http://localhost:8080/hello. You should see the string "Hello, World!" in the browser
16. Change the URL in the browser to http://localhost:8080/hello?name=Dolly. You should now see the string "Hello, Dolly!" in the browser
17. Shut down the application (there's no graceful way to do that -- just hit the stop button in your IDE)
18. Add a home page to the app by creating a file called `index.html` in the `src/main/resources/static` folder
19. The code for the `index.html` file is:

```html
<!DOCTYPE HTML>
<html lang="en">
<head>
    <title>Hello, World!</title>
</head>
<body>
    <h2>Say hello</h2>
    <form method="get" action="/hello">
        <label for="name">Name:</label>
        <input type="text" id="name" name="name"><br><br>
        <input type="submit" value="Say Hello">
    </form>
</body>
</html>
```

20. From a command prompt in the root of the project, build the application:

 > gradlew build

21. Now you can start the application with a generated executable jar file:

 > java -jar build/libs/demo-0.0.1-SNAPSHOT.jar

22. Navigate to http://localhost:8080 and see the new home page. From there you can navigate to the greeting page, and manually try adding a `name` parameter to the URL there
23. Again stop the application (use Ctrl-C in the command window)
24. Start it one more time using a special gradle task:

 > gradlew bootRun

25. When again you're happy the app is running properly, shut it down
26. Because the controller is a simple POJO, you can unit test it by simply instantiating the controller and calling its `sayHello` method directly. To do so, add a class called `HelloControllerUnitTest` to the `com.kousenit.demo.controllers` package in the _test_ folder, `src/test/java`
27. The code for the test class is:

```java
package com.kousenit.demo.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloControllerUnitTest {

    @Test
    public void sayHello() {
        HelloController controller = new HelloController();
        Model model = new BindingAwareModelMap();
        String result = controller.sayHello("World", model);
        assertAll(
                () -> assertEquals("World", model.getAttribute("user")),
                () -> assertEquals("welcome", result)
        );
    }
}
```

28. Run the test by executing this class as a JUnit test. It should pass. It's not terribly useful, however, since it isn't affected by the request mapping or the request parameter.
29. To perform an integration test instead, use the `MockMVC` classes available in Spring. Create a new class called `HelloControllerMockMVCTest` in the `com.kousenit.demo.controllers` package in `src/test/java`
30. The code for the integration test is:

```java
package com.kousenit.demo.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HelloController.class)
public class HelloControllerMockMVCTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void testHelloWithoutName() throws Exception {
        mvc.perform(get("/hello").accept(MediaType.TEXT_HTML))
           .andExpect(status().isOk())
           .andExpect(view().name("welcome"))
           .andExpect(model().attribute("user", "World"));

    }

    @Test
    public void testHelloWithName() throws Exception {
        mvc.perform(get("/hello").param("name", "Dolly").accept(MediaType.TEXT_HTML))
           .andExpect(status().isOk())
           .andExpect(view().name("welcome"))
           .andExpect(model().attribute("user", "Dolly"));
    }
}
```

31. The tests should pass successfully. One of the advantages of the `@WebMvcTest` annotation over the generic `@SpringBootTest` annotation is that it allows you to automatically inject an instance of `MockMvc`, as shown.

## Add a Rest Controller

1. Add another class to the `com.kousenit.demo.controllers` package called `HelloRestController`. This controller will be used to model a RESTful web service, though at this stage it will be limited to HTTP GET requests (for reasons explained below).
2. Add the `@RestController` annotation to the class.
3. By default, REST controllers will serialize and deserialize Java classes into JSON data using the Jackson 2 JSON library, which is currently on the classpath by default. To have an object (other than a trivial `String`) to serialize, add a class called `Greeting` to the `com.kousenit.demo.json` package. In a larger application, this would represent a domain class that you can store in a database or other persistent storage mechanism.
4. In the `Greeting` class, add a private attribute of type `String` called `message`.
5. Add a `getMessage` method for the `greeting` attribute that returns the current message.
6. Add a constructor to `Greeting` that takes a `String` argument and saves it to the attribute.
7. Add a default constructor that does nothing. This constructor will be used by the JSON parser to convert a JSON response into an instance of `Greeting`.
8. Add an `equals` method, a `hashCode` method, and a `toString` method in the usual manner. A reasonable version would be:

```java
package com.kousenit.demo.json;

import java.util.Objects;

public class Greeting {
    private String message;

    public Greeting() {}

    public Greeting(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Greeting)) return false;
        Greeting gr = (Greeting) o;
        return Objects.equals(message, gr.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return message;
    }
}
```

9. Back in the `HelloRestController`, add a method called `greet` that takes a `String` called `name` as an argument and returns a `Greeting`.
10. Annotate the `greet` method with a `@GetMapping` whose argument is `"/rest"`, which means that the URL to access the method will be http://localhost:8080/rest .
11. Add the `@RequestParam` annotation to the argument, with the properties `required` set to `false` and `defaultValue` set to `World`.
12. In the body of the method, return a new instance of `Greeting` whose constructor argument should be `"Hello, " + name + "!"`.
13. The full class looks like (note that the string concatenation has been replaced with a `String.format` method)

```java
package com.kousenit.demo.controllers;

import com.kousenit.demo.json.Greeting;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloRestController {

    @GetMapping("/rest")
    public Greeting greet(@RequestParam(required = false,
            defaultValue = "World") String name) {
        return new Greeting(String.format("Hello, %s!", name));
    }
}
```

14. You can now run the application and check the behavior using either `curl` or a similar command-line tool, or simply accessing the URL in a browser, either with or without a name.
15. To create a test for the REST controller, we will use the `TestRestTemplate` class, because we included the `web` dependency rather than `webflux` which we'll use in the next exercise. Add a class called `HelloRestControllerIntegrationTest` in the `src/test/java` tree in the same package as the REST controller class.
16. This time, when adding the `@SpringBootTest` annotation, add the argument `webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT`. This will autoconfigure several properties of the test, including making a `TestRestTemplate` available to inject.
17. Add two tests, one for greetings without a name and one for greetings with a name.
18. The tests should look like:

```java
@Test
public void greetWithName(@Autowired TestRestTemplate template) {
    Greeting response = template.getForObject("/rest?name=Dolly", Greeting.class);
    assertEquals("Hello, Dolly!", response.getMessage());
}

@Test
public void greetWithoutName(@Autowired TestRestTemplate template) {
    ResponseEntity<Greeting> entity = template.getForEntity("/rest", Greeting.class);
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(MediaType.APPLICATION_JSON, entity.getHeaders().getContentType());
    Greeting response = entity.getBody();
    if (response != null) {
        assertEquals("Hello, World!", response.getMessage());
    }
}
```

19. One test uses the `getForEntity` method of the template, which returns a `ResponseEntity<Greeting>`. The response entity gives access to the headers, so the two provided asserts check the status code and the media type of the response. The actual response is inside the body. By calling `getBody`, the response is returned as a de-serialized `Greeting` instance, which allows you to check its message.
20. The other test uses the `getForObject` method, which returns the de-serialized response directly. This is simpler, but does not allow access to the headers. You can use either approach in your code.
21. The tests should now pass. This application only checks HTTP GET requests, because the application doesn't have any way to save `Greeting` instances. Once that is added, you could include analogous POST, PUT, and DELETE operations.

## Building a REST client

This exercise uses both the modern `RestClient` class for synchronous access and the reactive `WebClient` for asynchronous access to RESTful web services. The `RestClient` is Spring's modern replacement for `RestTemplate`, providing a fluent API similar to `WebClient` but for synchronous calls. For reactive applications, `WebClient` returns responses of type `Mono` and `Flux`, which are essentially "promises" that return a single object (for `Mono`) or a collection (for `Flux`) of objects.

1. Create a new Spring Boot project (either by using the Initializr at http://start.spring.io or using your IDE) called `restclient`. Add both the _Spring Web_ and _Spring Reactive Web_ dependencies.
2. Create a service class called `AstroService` in a `com.kousenit.restclient.services` package under `src/main/java`

3. Add the annotation `@Service` to the class (from the `org.springframework.stereotype` package, so you'll need an `import` statement)

4. Add private attributes to `AstroService` of type `RestClient` called `restClient` and `WebClient` called `webClient`

5. Add a constructor to `AstroService` that takes no arguments. Inside the constructor, create both clients using their static factory methods:

   ```java
   public AstroService() {
       this.restClient = RestClient.create("http://api.open-notify.org");
       this.webClient = WebClient.create("http://api.open-notify.org");
   }
   ```

   > [!NOTE]
   > `RestClient` was introduced in Spring 6.1 as the modern replacement for `RestTemplate`. It provides a fluent API similar to `WebClient` but for synchronous operations.

6. The site providing the API is http://open-notify.org/, which is an API based on NASA data. We'll access the _Number of People in Space_ service using both synchronous and asynchronous approaches.

7. First, let's add a synchronous method using `RestClient`. Add a `public` method to our service called `getAstroResponseSync` that takes no arguments and returns a `String`:

   ```java
   public String getPeopleInSpace() {
       return restClient.get()
               .uri("/astros.json")
               .accept(MediaType.APPLICATION_JSON)
               .retrieve()
               .body(String.class);
   }
   ```

8. For proper object mapping, we'll need to create Java classes that map to the JSON structure. A typical example of the JSON response is:

```javascript
{
  "message": "success",
  "number": NUMBER_OF_PEOPLE_IN_SPACE,
  "people": [
    {"name": NAME, "craft": SPACECRAFT_NAME},
    ...
  ]
}
```

9. Each of the two JSON objects needs to be mapped to a class. Create classes `Assignment` and `AstroResponse` in the `com.kousenit.restclient.json` package. 

10. Using records (available in Java 17+), create these classes:

   ```java
   public record Assignment(String name, String craft) {
   }

   public record AstroResponse(String message, int number, List<Assignment> people) {
   }
   ```

11. Alternatively, if you prefer traditional classes, you can create these instead (though records are recommended for simple data classes):

```java
public class Assignment {
    private String name;
    private String craft;

    public Assignment() {
    }

    public Assignment(String name, String craft) {
        this.name = name;
        this.craft = craft;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCraft() {
        return craft;
    }

    public void setCraft(String craft) {
        this.craft = craft;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "name='" + name + '\'' +
                ", craft='" + craft + '\'' +
                '}';
    }
}
```

> **Note:** It is not actually necessary to map all the included fields, but the response is simple enough to do so in this case.

12. The `AstroResponse` class to accompany the `Assignment` class:

```java
package com.kousenit.restclient.json;

import java.util.List;

public class AstroResponse {
    private String message;
    private int number;
    private List<Assignment> people;

    public AstroResponse() {
    }

    public AstroResponse(String message, int number, List<Assignment> people) {
        this.message = message;
        this.number = number;
        this.people = people;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<Assignment> getPeople() {
        return people;
    }

    public void setPeople(List<Assignment> people) {
        this.people = people;
    }

    @Override
    public String toString() {
        return "AstroResponse{" +
                "message='" + message + '\'' +
                ", number=" + number +
                ", people=" + people +
                '}';
    }
}
```

13. Now update the JSON response methods to work with the `AstroResponse` class. Add a synchronous method using `RestClient`:

    ```java
    public AstroResponse getAstroResponseSync() {
        return restClient.get()
                .uri("/astros.json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(AstroResponse.class);
    }
    ```

14. For asynchronous access, add a method using `WebClient` that returns a `Mono`:

    ```java
    public Mono<AstroResponse> getAstroResponseAsync() {
        return webClient.get()
                .uri("/astros.json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AstroResponse.class)
                .log();
    }
    ```

    > [!NOTE]
    > The `log()` method will log all reactive stream interactions to the console, which is useful for debugging. In a production reactive application, you would typically return the `Mono` directly rather than blocking on it.

15. To demonstrate how to use the service, create a JUnit 5 test for it. Create a class called `AstroServiceTest` in the `com.kousenit.restclient.services` package under the test hierarchy, `src/test/java`.

16. Add tests for both synchronous and asynchronous methods:

```java
package com.kousenit.restclient.services;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AstroServiceTest {
    private final Logger logger = LoggerFactory.getLogger(AstroService.class);

    @Autowired
    private AstroService service;

    @Test
    void getAstroResponseSync() {
        AstroResponse response = service.getAstroResponseSync();
        logger.info(response.toString());
        assertNotNull(response);
        assertEquals("success", response.getMessage());
        assertTrue(response.getNumber() >= 0);
        assertEquals(response.getNumber(), response.getPeople().size());
    }

    @Test
    void getAstroResponseAsync() {
        AstroResponse response = service.getAstroResponseAsync()
                .block(Duration.ofSeconds(2));
        assertNotNull(response);
        assertEquals("success", response.getMessage());
        assertTrue(response.getNumber() >= 0);
        assertEquals(response.getNumber(), response.getPeople().size());
        logger.info(response.toString());
    }

    @Test
    void getAstroResponseAsyncWithStepVerifier() {
        service.getAstroResponseAsync()
                .as(StepVerifier::create)
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("success", response.getMessage());
                    assertTrue(response.getNumber() >= 0);
                    assertEquals(response.getNumber(), response.getPeople().size());
                    logger.info(response.toString());
                })
                .verifyComplete();
    }
}
```

17. You'll need to add these imports to the test class:

    ```java
    import reactor.test.StepVerifier;
    import java.time.Duration;
    import static org.junit.jupiter.api.Assertions.*;
    ```

18. Note the use of the SLF4J `Logger` class to log the responses to the console. The reactive test also demonstrates `StepVerifier`, which is the preferred way to test reactive streams.

19. If you used records for the JSON classes, replace method calls like `getMessage()` with `message()`, `getNumber()` with `number()`, and `getPeople()` with `people()`.

20. Execute the tests and make any needed corrections until they pass.

[Back to Table of Contents](#table-of-contents)

## HTTP Interfaces (Spring Boot 3+)

Spring Boot 3.0 introduced HTTP Interfaces, a declarative way to access external RESTful web services. The [Spring 6 documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#spring-integration) has a section on REST clients, which includes the `RestTemplate` and `WebClient` classes discussed above, as well as something called HTTP Interface.

The idea is to declare an interface with the access methods you want, and add a proxy factory bean to the application context, and Spring will implement the interface methods for you. This exercise is a quick example of how to do that for our current application.

1. Add an interface called `AstroInterface` to the `services` package.
2. Inside that interface, add a method to perform an HTTP GET request to our "People In Space" endpoint:

```java
public interface AstroInterface {
    @GetExchange("/astros.json")
    Mono<AstroResponse> getAstroResponse();
}
```

3. Like most publicly available services, this service only supports GET requests. For those that support other HTTP methods, there are annotations `@PutExchange`, `@PostExchange`, `@DeleteExchange`, and so on. Also, this particular request does not take any parameters, so it is particularly simple. If it took parameters, they would appear in the URL at Http Template variables, and in the parameter list of the method annotated with `@PathVariable` or something similar.
4. We now need the proxy factory bean, which goes in a Java configuration class. Since the `RestClientApplication` class (the class with the standard Java `main` method) is annotated with `@SpringBootApplication`, it ultimately contains the annotation `@Configuration`. That means we can add `@Bean` methods to it, which Spring will use to add beans to the application context. Therefore, add the following bean to that class:

```java
@Bean
public AstroInterface astroInterface() {
    WebClient client = WebClient.create("http://api.open-notify.org/");
    HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client)).build();

    return factory.createClient(AstroInterface.class);
}
```

5. That method creates a `WebClient` configured for the base URL, and uses that to build an `HttpServiceProxyFactory`. From the factory, we use the `createClient` method to tell Spring to create a class that implements the `AstroInterface`.
6. To test this, simply reuse the `AstroServiceTest` class by adding another test:

```java
@Test
void getAstroResponseFromInterface(@Autowired AstroInterface astroInterface) {
    AstroResponse response = astroInterface.getAstroResponse()
            .block(Duration.ofSeconds(2));
    assertNotNull(response);
    assertAll(
            () -> assertEquals("success", response.message()),
            () -> assertTrue(response.number() >= 0),
            () -> assertEquals(response.number(), response.people().size())
    );
    System.out.println(response);
}
```

7. That test should pass. Note that for synchronous access, simply change the return type of the method inside the `getAstroResponse` method of `AstroInterface` to `AstroResponse` instead of the `Mono`. See the documentation for additional details.

## Consuming External APIs

This exercise demonstrates how to consume external RESTful APIs using both `RestClient` and `WebClient`. We'll use the JSON Placeholder API (https://jsonplaceholder.typicode.com/), a free testing service designed for prototyping and learning.

> [!NOTE]
> JSON Placeholder provides realistic fake data with endpoints for posts, comments, albums, photos, todos, and users. It's perfect for learning API consumption without API keys or quotas.

1. Rather than creating a new project, we'll add services to the existing `restclient` project. The JSON Placeholder API provides several endpoints, but we'll focus on `/users` and `/posts` as they demonstrate different patterns.

2. First, let's examine the JSON structure. A typical user response from `https://jsonplaceholder.typicode.com/users/1` looks like:

   ```json
   {
     "id": 1,
     "name": "Leanne Graham",
     "username": "Bret",
     "email": "Sincere@april.biz",
     "address": {
       "street": "Kulas Light",
       "suite": "Apt. 556",
       "city": "Gwenborough",
       "zipcode": "92998-3874",
       "geo": {
         "lat": "-37.3159",
         "lng": "81.1496"
       }
     },
     "phone": "1-770-736-8031 x56442",
     "website": "hildegard.org",
     "company": {
       "name": "Romaguera-Crona",
       "catchPhrase": "Multi-layered client-server neural-network",
       "bs": "harness real-time e-markets"
     }
   }
   ```

3. Create domain classes using records in the `com.kousenit.restclient.json` package. Since this demonstrates nested JSON objects, we'll create multiple records:

   ```java
   package com.kousenit.restclient.json;

   public record Address(
           String street,
           String suite, 
           String city,
           String zipcode,
           Geo geo
   ) {}

   public record Geo(String lat, String lng) {}

   public record Company(
           String name,
           String catchPhrase,
           String bs
   ) {}

   public record User(
           Long id,
           String name,
           String username,
           String email,
           Address address,
           String phone,
           String website,
           Company company
   ) {}
   ```

4. For posts, the JSON structure is simpler. A typical post from `https://jsonplaceholder.typicode.com/posts/1`:

   ```json
   {
     "userId": 1,
     "id": 1,
     "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
     "body": "quia et suscipit\nsuscipit recusandae consequuntur..."
   }
   ```

   Create a record for posts:

   ```java
   public record Post(
           Long userId,
           Long id,
           String title,
           String body
   ) {}
   ```

5. Create a service called `JsonPlaceholderService` in the `com.kousenit.restclient.services` package:

   ```java
   @Service
   public class JsonPlaceholderService {
       private final RestClient restClient;
       private final WebClient webClient;

       public JsonPlaceholderService() {
           this.restClient = RestClient.create("https://jsonplaceholder.typicode.com");
           this.webClient = WebClient.create("https://jsonplaceholder.typicode.com");
       }

       // ... methods to come
   }
   ```

6. Add synchronous methods using `RestClient`:

   ```java
   public List<User> getAllUsersSync() {
       return restClient.get()
               .uri("/users")
               .accept(MediaType.APPLICATION_JSON)
               .retrieve()
               .body(new ParameterizedTypeReference<List<User>>() {});
   }

   public Optional<User> getUserByIdSync(Long id) {
       try {
           User user = restClient.get()
                   .uri("/users/{id}", id)
                   .accept(MediaType.APPLICATION_JSON)
                   .retrieve()
                   .body(User.class);
           return Optional.ofNullable(user);
       } catch (Exception e) {
           return Optional.empty();
       }
   }

   public List<Post> getPostsByUserIdSync(Long userId) {
       return restClient.get()
               .uri("/users/{userId}/posts", userId)
               .accept(MediaType.APPLICATION_JSON)
               .retrieve()
               .body(new ParameterizedTypeReference<List<Post>>() {});
   }
   ```

7. Add asynchronous methods using `WebClient`:

   ```java
   public Flux<User> getAllUsersAsync() {
       return webClient.get()
               .uri("/users")
               .accept(MediaType.APPLICATION_JSON)
               .retrieve()
               .bodyToFlux(User.class)
               .log();
   }

   public Mono<User> getUserByIdAsync(Long id) {
       return webClient.get()
               .uri("/users/{id}", id)
               .accept(MediaType.APPLICATION_JSON)
               .retrieve()
               .bodyToMono(User.class)
               .log();
   }

   public Flux<Post> getPostsByUserIdAsync(Long userId) {
       return webClient.get()
               .uri("/users/{userId}/posts", userId)
               .accept(MediaType.APPLICATION_JSON)
               .retrieve()
               .bodyToFlux(Post.class)
               .log();
   }
   ```

8. Add POST methods for creating new resources. Note that JSON Placeholder simulates creation but doesn't persist data:

   ```java
   // POST methods - Create new resources
   public User createUserSync(User user) {
       return restClient.post()
               .uri("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .accept(MediaType.APPLICATION_JSON)
               .body(user)
               .retrieve()
               .body(User.class);
   }

   public Post createPostSync(Post post) {
       return restClient.post()
               .uri("/posts")
               .contentType(MediaType.APPLICATION_JSON)
               .accept(MediaType.APPLICATION_JSON)
               .body(post)
               .retrieve()
               .body(Post.class);
   }

   public Mono<User> createUserAsync(User user) {
       return webClient.post()
               .uri("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .accept(MediaType.APPLICATION_JSON)
               .bodyValue(user)
               .retrieve()
               .bodyToMono(User.class)
               .log();
   }

   public Mono<Post> createPostAsync(Post post) {
       return webClient.post()
               .uri("/posts")
               .contentType(MediaType.APPLICATION_JSON)
               .accept(MediaType.APPLICATION_JSON)
               .bodyValue(post)
               .retrieve()
               .bodyToMono(Post.class)
               .log();
   }
   ```

9. Add DELETE methods for removing resources:

   ```java
   // DELETE methods - Remove resources
   public void deleteUserSync(Long id) {
       restClient.delete()
               .uri("/users/{id}", id)
               .retrieve()
               .toBodilessEntity();
   }

   public void deletePostSync(Long id) {
       restClient.delete()
               .uri("/posts/{id}", id)
               .retrieve()
               .toBodilessEntity();
   }

   public Mono<Void> deleteUserAsync(Long id) {
       return webClient.delete()
               .uri("/users/{id}", id)
               .retrieve()
               .bodyToMono(Void.class)
               .log();
   }

   public Mono<Void> deletePostAsync(Long id) {
       return webClient.delete()
               .uri("/posts/{id}", id)
               .retrieve()
               .bodyToMono(Void.class)
               .log();
   }
   ```

10. You'll need these imports for the generic collections:

   ```java
   import org.springframework.core.ParameterizedTypeReference;
   import reactor.core.publisher.Flux;
   import reactor.core.publisher.Mono;
   ```

9. Create a comprehensive test class called `JsonPlaceholderServiceTest`:

   ```java
   @SpringBootTest
   class JsonPlaceholderServiceTest {
       private final Logger logger = LoggerFactory.getLogger(JsonPlaceholderServiceTest.class);

       @Autowired
       private JsonPlaceholderService service;

       @Test
       void getAllUsersSync() {
           List<User> users = service.getAllUsersSync();
           assertNotNull(users);
           assertFalse(users.isEmpty());
           assertEquals(10, users.size()); // JSON Placeholder has 10 users
           
           User firstUser = users.get(0);
           assertNotNull(firstUser.name());
           assertNotNull(firstUser.email());
           assertNotNull(firstUser.address());
           assertNotNull(firstUser.company());
           
           logger.info("First user: {}", firstUser);
       }

       @Test
       void getUserByIdSync() {
           Optional<User> userOpt = service.getUserByIdSync(1L);
           assertTrue(userOpt.isPresent());
           
           User user = userOpt.get();
           assertEquals(1L, user.id());
           assertEquals("Leanne Graham", user.name());
           assertNotNull(user.address().geo());
           
           logger.info("User 1: {}", user);
       }

       @Test
       void getUserByIdSyncNotFound() {
           Optional<User> userOpt = service.getUserByIdSync(999L);
           assertFalse(userOpt.isPresent());
       }

       @Test
       void getPostsByUserIdSync() {
           List<Post> posts = service.getPostsByUserIdSync(1L);
           assertNotNull(posts);
           assertFalse(posts.isEmpty());
           assertEquals(10, posts.size()); // Each user has 10 posts
           
           posts.forEach(post -> {
               assertEquals(1L, post.userId());
               assertNotNull(post.title());
               assertNotNull(post.body());
           });
           
           logger.info("User 1 has {} posts", posts.size());
       }

       @Test
       void getAllUsersAsync() {
           List<User> users = service.getAllUsersAsync()
                   .collectList()
                   .block(Duration.ofSeconds(5));
                   
           assertNotNull(users);
           assertEquals(10, users.size());
           logger.info("Retrieved {} users asynchronously", users.size());
       }

       @Test
       void getUserByIdAsync() {
           User user = service.getUserByIdAsync(1L)
                   .block(Duration.ofSeconds(5));
                   
           assertNotNull(user);
           assertEquals("Leanne Graham", user.name());
           logger.info("User retrieved asynchronously: {}", user.name());
       }

       @Test
       void getAllUsersAsyncWithStepVerifier() {
           service.getAllUsersAsync()
                   .as(StepVerifier::create)
                   .expectNextCount(10)
                   .verifyComplete();
       }

       @Test
       void getComplexUserData() {
           // Demonstrate working with nested objects
           Optional<User> userOpt = service.getUserByIdSync(1L);
           assertTrue(userOpt.isPresent());
           
           User user = userOpt.get();
           Address address = user.address();
           Company company = user.company();
           
           assertNotNull(address.city());
           assertNotNull(address.geo().lat());
           assertNotNull(company.name());
           
           logger.info("User {} lives in {} and works at {}", 
                   user.name(), address.city(), company.name());
       }

       @Test
       void createUserSync() {
           User newUser = createTestUser();
           
           User createdUser = service.createUserSync(newUser);
           
           assertNotNull(createdUser);
           assertNotNull(createdUser.id()); // JSON Placeholder assigns an ID
           assertEquals(newUser.name(), createdUser.name());
           assertEquals(newUser.email(), createdUser.email());
           
           logger.info("Created user: {}", createdUser);
       }

       @Test
       void createPostSync() {
           Post newPost = new Post(1L, null, "Test Post Title", "This is a test post body");
           
           Post createdPost = service.createPostSync(newPost);
           
           assertNotNull(createdPost);
           assertNotNull(createdPost.id()); // JSON Placeholder assigns an ID
           assertEquals(newPost.title(), createdPost.title());
           assertEquals(newPost.body(), createdPost.body());
           assertEquals(newPost.userId(), createdPost.userId());
           
           logger.info("Created post: {}", createdPost);
       }

       @Test
       void createUserAsync() {
           User newUser = createTestUser();
           
           service.createUserAsync(newUser)
                   .as(StepVerifier::create)
                   .expectNextMatches(user -> 
                           user.name().equals("John Tester") && 
                           user.email().equals("john@test.com"))
                   .verifyComplete();
       }

       @Test
       void createPostAsync() {
           Post newPost = new Post(1L, null, "Async Test Post", "This is an async test post");
           
           service.createPostAsync(newPost)
                   .as(StepVerifier::create)
                   .expectNextMatches(post -> 
                           post.title().equals("Async Test Post") && 
                           post.userId().equals(1L))
                   .verifyComplete();
       }

       @Test
       void deleteUserSync() {
           // JSON Placeholder sometimes has timeout issues with DELETE, so we test the method exists
           // and handles the request properly, even if it times out
           try {
               service.deleteUserSync(1L);
               logger.info("Delete user 1 completed successfully");
           } catch (Exception e) {
               // Expect either success or timeout - both are acceptable for this demo
               assertTrue(e.getMessage().contains("timeout") || e.getMessage().contains("I/O error"),
                       "Unexpected error type: " + e.getMessage());
               logger.info("Delete user 1 timed out (expected behavior with JSON Placeholder)");
           }
       }

       @Test
       void deletePostSync() {
           try {
               service.deletePostSync(1L);
               logger.info("Delete post 1 completed successfully");
           } catch (Exception e) {
               // Expect either success or timeout - both are acceptable for this demo
               assertTrue(e.getMessage().contains("timeout") || e.getMessage().contains("I/O error"),
                       "Unexpected error type: " + e.getMessage());
               logger.info("Delete post 1 timed out (expected behavior with JSON Placeholder)");
           }
       }

       @Test
       void deleteUserAsync() {
           // For async deletes, we'll test with a timeout and handle potential network issues
           try {
               service.deleteUserAsync(1L)
                       .timeout(Duration.ofSeconds(10))
                       .block();
               logger.info("Delete user 1 asynchronously completed");
           } catch (Exception e) {
               // Handle timeout or network issues gracefully
               assertTrue(e.getCause() instanceof java.util.concurrent.TimeoutException || 
                         e.getMessage().contains("timeout") ||
                         e.getMessage().contains("I/O error"),
                       "Unexpected error type: " + e.getMessage());
               logger.info("Delete user 1 async timed out (expected behavior with JSON Placeholder)");
           }
       }

       @Test
       void deletePostAsync() {
           try {
               service.deletePostAsync(1L)
                       .timeout(Duration.ofSeconds(10))
                       .block();
               logger.info("Delete post 1 asynchronously completed");
           } catch (Exception e) {
               // Handle timeout or network issues gracefully
               assertTrue(e.getCause() instanceof java.util.concurrent.TimeoutException || 
                         e.getMessage().contains("timeout") ||
                         e.getMessage().contains("I/O error"),
                       "Unexpected error type: " + e.getMessage());
               logger.info("Delete post 1 async timed out (expected behavior with JSON Placeholder)");
           }
       }

       private User createTestUser() {
           Geo geo = new Geo("-37.3159", "81.1496");
           Address address = new Address("123 Test St", "Test Suite", "Testville", "12345", geo);
           Company company = new Company("Test Corp", "Testing is our business", "test");
           
           return new User(null, "John Tester", "john.tester", "john@test.com", address, "555-1234", "johntester.com", company);
       }
   }
   ```

10. Add the required imports to your test class:

    ```java
    import reactor.test.StepVerifier;
    import java.time.Duration;
    import java.util.Optional;
    import static org.junit.jupiter.api.Assertions.*;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    ```

11. Run the tests to verify the integration works correctly. All tests should pass, demonstrating successful API consumption.

## Key Learning Points

This exercise demonstrates several important concepts:

- **No API Keys Required**: JSON Placeholder removes authentication complexity
- **Nested JSON Mapping**: Records handle complex nested structures elegantly  
- **Path Parameters**: Using `{id}` placeholders in URIs
- **Error Handling**: Graceful handling of non-existent resources
- **Synchronous vs Asynchronous**: Comparing `RestClient` and `WebClient` approaches
- **Type Safety**: Generic collections with `ParameterizedTypeReference`
- **Reactive Testing**: Using `StepVerifier` for reactive streams
- **HTTP Methods**: Complete CRUD operations with GET, POST, and DELETE
- **Content Types**: Setting proper `Content-Type` and `Accept` headers
- **Request Bodies**: Sending JSON data with POST requests

> [!TIP]
> JSON Placeholder is perfect for prototyping and testing. It supports GET, POST, PUT, PATCH, and DELETE methods (though modifications aren't persisted), making it ideal for learning REST patterns.

[Back to Table of Contents](#table-of-contents)

## Using the JDBC template

Spring provides a class called `JdbcTemplate` in the `org.springframework.jdbc.core` package. All it needs in order to work is a data source. It removes almost all the boilerplate code normally associated with JDBC. In this exercise, you'll use the `JdbcTemplate` to implement the standard CRUD (create, read, update, delete) methods on an entity.

1. Make a new Spring Boot project with group `com.kousenit` and artifact called `persistence` using the Spring Initializr. Generate a Gradle build file and select the JPA dependency, which will include JDBC. Also select the H2 dependency, which will provide a JDBC driver for the H2 database as well as a connection pool.
2. Import the project into your IDE in the usual manner.
3. For this exercise, as well as the related exercises using JPA and Spring Data, we'll use a domain class called `Officer`. An `Officer` will have a generated `id` of type `Integer`, strings for `firstName` and `lastName`, and a `Rank`. The `Rank` will be a Java enum.
4. First define the `Rank` enum in the `com.kousenit.persistence.entities` package and give it a few constants:

```java
public enum Rank {
    ENSIGN, LIEUTENANT, COMMANDER, CAPTAIN, COMMODORE, ADMIRAL
}
```

5. Now add the `Officer` class with the attributes as specified.

```java
public class Officer {
    private Integer id;
    private Rank rank;
    private String firstName;
    private String lastName;

    public Officer() {}

    public Officer(Rank rank, String firstName, String lastName) {
        this.rank = rank;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Officer(Integer id, Rank rank, String firstName, String lastName) {
        this.id = id;
        this.rank = rank;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "Officer{" +
                "id=" + id +
                ", rank=" + rank +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Officer)) return false;

        Officer officer = (Officer) o;

        if (!id.equals(officer.id)) return false;
        if (rank != officer.rank) return false;
        if (firstName != null ? !firstName.equals(officer.firstName) : officer.firstName != null) return false;
        return lastName.equals(officer.lastName);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + rank.hashCode();
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + lastName.hashCode();
        return result;
    }
}
```

6. One of the features of Spring Boot is that you can create and populate database tables by defining scripts with the names `schema.sql` and `data.sql` in the `src/main/resources` folder. First define the database table in `schema.sql`:

   ```sql
   DROP TABLE IF EXISTS officers;
   CREATE TABLE officers (
     id         INT         NOT NULL AUTO_INCREMENT,
     rank       VARCHAR(20) NOT NULL,
     first_name VARCHAR(50) NOT NULL,
     last_name  VARCHAR(50) NOT NULL,
     PRIMARY KEY (id)
   );
   ```

7. Next populate the table by adding the following `INSERT` statements in `data.sql`:

   ```sql
   INSERT INTO officers(rank, first_name, last_name) VALUES('CAPTAIN', 'James', 'Kirk');
   INSERT INTO officers(rank, first_name, last_name) VALUES('CAPTAIN', 'Jean-Luc', 'Picard');
   INSERT INTO officers(rank, first_name, last_name) VALUES('CAPTAIN', 'Benjamin', 'Sisko');
   INSERT INTO officers(rank, first_name, last_name) VALUES('CAPTAIN', 'Kathryn', 'Janeway');
   INSERT INTO officers(rank, first_name, last_name) VALUES('CAPTAIN', 'Jonathan', 'Archer');
   ```

8. When Spring starts up, the framework will automatically create a DB connection pool based on the H2 driver and then create and populate the database tables for you. Now we need a DAO (data access object) interface holding the CRUD methods that will be implemented in the different technologies. Define a Java interface called `OfficerDAO` in the `com.kousenit.persistence.dao` package.

```java
package com.kousenit.persistence.dao;

import com.kousenit.persistence.entities.Officer;

import java.util.List;
import java.util.Optional;

public interface OfficerDAO {
    Officer save(Officer officer);
    Optional<Officer> findById(Integer id);
    List<Officer> findAll();
    long count();
    void delete(Officer officer);
    boolean existsById(Integer id);
}
```

As an aside, the names and signatures of these methods were chosen for a reason, which will become obvious when you do the Spring Data implementation later

9. In this exercise, implement the interface using the `JdbcTemplate` class. Start by creating a class in the `com.kousenit.persistence.dao` package called `JdbcOfficerDAO`.
10. Normally in Spring you would create an instance of `JdbcTemplate` by injecting a `DataSource` into the constructor and using it to instantiate the `JdbcTemplate`. Spring Boot, however, let's you inject a `JdbcTemplate` directly.

```java
public class JdbcOfficerDAO implements OfficerDAO {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcOfficerDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

// ... more to come ...

}
```

11. To make Spring detect this as a bean it should manage, add the `@Repository` annotation to the class

```java
@Repository
public class JdbcOfficerDAO implements OfficerDAO {
    // ... as before ...
}
```

12. Some of the DAO methods are trivially easy to implement. Implement the `count` method by executing a `queryForObject` that uses a `SELECT count(*)` SQL statement and maps the result to a long.

```java
@Override
public long count() {
    return jdbcTemplate.queryForObject(
        "select count(*) from officers", Long.class);
}
```

13. Likewise, the `delete` method is easy to implement using the `update` method from the template class. The interesting part is that by putting a `?` wildcard in the SQL statement, the template will automatically use a `PreparedStatement` to execute the SQL

```java
@Override
public void delete(Officer officer) {
    jdbcTemplate.update("DELETE FROM officers WHERE id=?", officer.getId());
}
```

14. The `exists` method also uses a `PreparedStatement` with an `id`, but this time the result should be mapped to a boolean.

```java
@Override
public boolean existsById(Integer id) {
    return jdbcTemplate.queryForObject(
        "SELECT EXISTS(SELECT 1 FROM officers where id=?)", Boolean.class, id);
}
```

15. Now for the finder methods. When a SQL query produces a `ResultSet`, the template asks for an implementation of the `RowMapper` interface as another argument to the `queryForObject` method. This interface has a single abstract method called `mapRow`, which takes the `ResultSet` and a row number as arguments. The implementation converts a row of the result set into an instance of the domain class. Implement the `findById` method using a `RowMapper`:

```java
@Override
public Optional<Officer> findById(Integer id) {
    try (Stream<Officer> stream =
            jdbcTemplate.queryForStream(
                "select * from officers where id=?",
                (rs, rowNum) -> new Officer(rs.getInt("id"),
                        Rank.valueOf(rs.getString("rank")),
                        rs.getString("first_name"),
                        rs.getString("last_name")),
                id)) {
        return stream.findFirst();
    }
}
```

16. The same row mapper pattern can be used to find all instances of `Officer`. The `JdbcTemplate` uses the `query` method to automatically iterate over the result set, calling the row mapper for each row to convert it to an `Officer`, and returns a collection of officers.

```java
@Override
public List<Officer> findAll() {
    return jdbcTemplate.query("SELECT * FROM officers",
            (rs, rowNum) -> new Officer(rs.getInt("id"),
                    Rank.valueOf(rs.getString("rank")),
                    rs.getString("first_name"),
                    rs.getString("last_name")));
}
```

The row mapper implementation uses a lambda expression for clean, concise code.

17. Finally, for the insert, we'll take a different approach. While you can write the SQL insert statement and use the `update` method on the `JdbcTemplate`, there is no easy way to return the generated primary key. So instead let's use a related class called a `SimpleJdbcInsert`. Add that class as an attribute and instantiate and configure it in the constructor

```java
public class JdbcOfficerDAO implements OfficerDAO {
    // ... jdbcTemplate from earlier ...
    private SimpleJdbcInsert insertOfficer;

    @Autowired
    public JdbcOfficerDAO(JdbcTemplate jdbcTemplate) {
        // ... jdbcTemplate from earlier ...
        insertOfficer = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("officers")
                .usingGeneratedKeyColumns("id");
    }
```

Note how you can specify the table that the insert will use, as well as any generated key columns.

18. Implement the  `save` method using the `SimpleJdbcInsert` instance

```java
@Override
public Officer save(Officer officer) {
    Map<String,Object> parameters = new HashMap<>();
    parameters.put("rank", officer.getRank());
    parameters.put("first_name", officer.getFirstName());
    parameters.put("last_name", officer.getLastName());
    Integer newId = (Integer) insertOfficer.executeAndReturnKey(parameters);
    officer.setId(newId);
    return officer;
}
```

Notice the typical Spring approach: there is an interface in the library called `SqlParameterSource` along with several implementation classes, one of which is `MapSqlParameterSource`. Any of them can be used as the argument to the `executeAndReturnKey` method. In this case, the easiest option is to use a simple `HashMap`.

19. We need a test case to make sure everything is working properly. Create a test class called `JdbcOfficerDAOTest` that autowires in the DAO class

```java
@SpringBootTest
public class JdbcOfficerDAOTest {
    @Autowired
    private OfficerDAO dao;

// ... more to come ...
}
```

20. Now comes the fun part -- add the `@Transactional` annotation to the class. In a test class like this, Spring will interpret that to mean that each test should run in a transaction that _rolls back at the end of the test_. That will keep the test database from being affected by the tests and keep the tests themselves all independent
21. Add a test for the `save` method

```java
@Test
public void save() throws Exception {
    Officer officer = new Officer(Rank.LIEUTENANT, "Nyota", "Uhuru");
    officer = dao.save(officer);
    assertNotNull(officer.getId());
}
```

The presence of the `@Transactional` annotation means that the new officer will be added, and we can check that the `id` value is correctly generated, but at the end of the test the insert will be rolled back

22. Test `findById` but using one of the known ids (which are known because the database is being reset each time)

```java
@Test
public void findByIdThatExists() throws Exception {
    Optional<Officer> officer = dao.findById(1);
    assertTrue(officer.isPresent());
    assertEquals(1, officer.get().getId().intValue());
}

@Test
public void findByIdThatDoesNotExist() throws Exception {
    Optional<Officer> officer = dao.findById(999);
    assertFalse(officer.isPresent());
}
```

23. The test for the count method also relies on knowing the number of rows in the test database

```java
@Test
public void count() throws Exception {
    assertEquals(5, dao.count());
}
```

24. The rest of the tests are straightforward and use modern Java features:

```java
@Test
public void findAll() throws Exception {
    List<String> dbNames = dao.findAll().stream()
            .map(Officer::getLastName)
            .collect(Collectors.toList());
    assertThat(dbNames).contains("Kirk", "Picard", "Sisko", "Janeway", "Archer");
}

@Test
public void delete() throws Exception {
    IntStream.rangeClosed(1, 5)
            .forEach(id -> {
                Optional<Officer> officer = dao.findById(id);
                assertTrue(officer.isPresent());
                dao.delete(officer.get());
            });
    assertEquals(0, dao.count());
}

@Test
public void existsById() throws Exception {
    IntStream.rangeClosed(1, 5)
            .forEach(id -> assertTrue(dao.existsById(id)));
}
```

We'll talk about the details of these tests in class. Note, however, that the test for `delete` removes all the officers from the table and verifies that they're gone. That would be a problem, except for, once again, the automatic rollback we're relying on at the end of each test.

25. Make sure all the tests work properly, then you're finished.

26. The SQL code executed has been provided, with one exception -- the `INSERT` statement generated by the `SimpleJdbcInsert`. To see it, you can log it to the console. In the file `application.properties` in `src/main/resoures`, add the following line:

```java
logging.level.sql=debug
```

This will enable logging for that specific class. You can use the logger for many parts of the underlying system, including the embedded container, Hibernate, and Spring Boot.

[Back to Table of Contents](#table-of-contents)

## Using the JDBC Client (Spring Boot 3.2+)

Spring Framework 6.1 (included in Spring Boot 3.2+) introduced `JdbcClient`, a modern fluent API that serves as a more user-friendly alternative to `JdbcTemplate`. While `JdbcTemplate` remains widely used and fully supported, `JdbcClient` provides a cleaner, more readable approach that aligns with other modern Spring APIs like `RestClient` and `WebClient`.

> [!NOTE]
> This exercise uses the same database schema and `Officer` entity from the previous JdbcTemplate lab. You can continue with the same `persistence` project or create a new one following the same setup steps.

1. Create a new DAO implementation called `JdbcClientOfficerDAO` that implements the same `OfficerDAO` interface, but uses `JdbcClient` instead of `JdbcTemplate`.

2. Add the `@Repository` annotation and inject `JdbcClient` into the constructor:

   ```java
   @Repository
   public class JdbcClientOfficerDAO implements OfficerDAO {
       private final JdbcClient jdbcClient;

       public JdbcClientOfficerDAO(JdbcClient jdbcClient) {
           this.jdbcClient = jdbcClient;
       }

       // ... methods to come
   }
   ```

   > [!NOTE]
   > Spring Boot automatically provides a `JdbcClient` bean when the JDBC dependency is present, configured with the same `DataSource` as `JdbcTemplate`.

3. Implement the `count` method using the fluent API:

   ```java
   @Override
   public long count() {
       return jdbcClient.sql("SELECT count(*) FROM officers")
               .query(Long.class)
               .single();
   }
   ```

4. Implement the `existsById` method with named parameters:

   ```java
   @Override
   public boolean existsById(Integer id) {
       return jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM officers WHERE id = :id)")
               .param("id", id)
               .query(Boolean.class)
               .single();
   }
   ```

5. Implement the `delete` method:

   ```java
   @Override
   public void delete(Officer officer) {
       jdbcClient.sql("DELETE FROM officers WHERE id = :id")
               .param("id", officer.getId())
               .update();
   }
   ```

6. For the finder methods, `JdbcClient` provides clean row mapping. Implement `findById`:

   ```java
   @Override
   public Optional<Officer> findById(Integer id) {
       return jdbcClient.sql("SELECT * FROM officers WHERE id = :id")
               .param("id", id)
               .query((rs, rowNum) -> new Officer(
                       rs.getInt("id"),
                       Rank.valueOf(rs.getString("rank")),
                       rs.getString("first_name"),
                       rs.getString("last_name")))
               .optional();
   }
   ```

7. Implement `findAll` using the same row mapper pattern:

   ```java
   @Override
   public List<Officer> findAll() {
       return jdbcClient.sql("SELECT * FROM officers")
               .query((rs, rowNum) -> new Officer(
                       rs.getInt("id"),
                       Rank.valueOf(rs.getString("rank")),
                       rs.getString("first_name"),
                       rs.getString("last_name")))
               .list();
   }
   ```

8. For the `save` method, we'll use `JdbcClient` with a key holder to get the generated ID. First, add the required import and create a key holder:

   ```java
   @Override
   public Officer save(Officer officer) {
       if (officer.getId() == null) {
           // Insert new officer
           var keyHolder = new GeneratedKeyHolder();
           jdbcClient.sql("""
                   INSERT INTO officers (rank, first_name, last_name) 
                   VALUES (:rank, :firstName, :lastName)
                   """)
                   .param("rank", officer.getRank().name())
                   .param("firstName", officer.getFirstName())
                   .param("lastName", officer.getLastName())
                   .update(keyHolder);
           
           var newId = keyHolder.getKey().intValue();
           return new Officer(newId, officer.getRank(), 
                   officer.getFirstName(), officer.getLastName());
       } else {
           // Update existing officer
           jdbcClient.sql("""
                   UPDATE officers 
                   SET rank = :rank, first_name = :firstName, last_name = :lastName 
                   WHERE id = :id
                   """)
                   .param("id", officer.getId())
                   .param("rank", officer.getRank().name())
                   .param("firstName", officer.getFirstName())
                   .param("lastName", officer.getLastName())
                   .update();
           return officer;
       }
   }
   ```

   > [!TIP]
   > Notice how `JdbcClient` allows us to use text blocks (Java 17+ feature) for multi-line SQL, making the code more readable. The named parameters (`:paramName`) are much cleaner than positional parameters (`?`).

9. Add the necessary imports to your class:

   ```java
   import org.springframework.jdbc.core.simple.JdbcClient;
   import org.springframework.jdbc.support.GeneratedKeyHolder;
   ```

10. Create a test class called `JdbcClientOfficerDAOTest` to verify the implementation. Use the same test patterns as the `JdbcTemplate` version, but with a different qualifier:

    ```java
    @SpringBootTest
    @Transactional
    public class JdbcClientOfficerDAOTest {
        
        @Autowired
        @Qualifier("jdbcClientOfficerDAO")
        private OfficerDAO dao;

        @Test
        public void save() {
            Officer officer = new Officer(Rank.LIEUTENANT, "Nyota", "Uhura");
            officer = dao.save(officer);
            assertNotNull(officer.getId());
        }

        @Test
        public void findByIdThatExists() {
            Optional<Officer> officer = dao.findById(1);
            assertTrue(officer.isPresent());
            assertEquals(1, officer.get().getId().intValue());
        }

        @Test
        public void findByIdThatDoesNotExist() {
            Optional<Officer> officer = dao.findById(999);
            assertFalse(officer.isPresent());
        }

        @Test
        public void count() {
            assertEquals(5, dao.count());
        }

        @Test
        public void findAll() {
            List<String> dbNames = dao.findAll().stream()
                    .map(Officer::getLastName)
                    .collect(Collectors.toList());
            assertThat(dbNames).contains("Kirk", "Picard", "Sisko", "Janeway", "Archer");
        }

        @Test
        public void delete() {
            IntStream.rangeClosed(1, 5)
                    .forEach(id -> {
                        Optional<Officer> officer = dao.findById(id);
                        assertTrue(officer.isPresent());
                        dao.delete(officer.get());
                    });
            assertEquals(0, dao.count());
        }

        @Test
        public void existsById() {
            IntStream.rangeClosed(1, 5)
                    .forEach(id -> assertTrue(dao.existsById(id)));
        }
    }
    ```

11. Run the tests to verify that the `JdbcClient` implementation works correctly. All tests should pass, demonstrating that both approaches provide the same functionality.

## Key Advantages of JdbcClient

The `JdbcClient` approach offers several benefits over `JdbcTemplate`:

- **Fluent API**: More readable and chainable method calls
- **Named Parameters**: Clearer parameter binding with `:paramName` syntax
- **Built-in Optional Support**: Methods like `optional()` and `single()` provide better null handling
- **Consistent Design**: Follows the same patterns as other modern Spring clients
- **Text Block Friendly**: Works seamlessly with Java 17+ text blocks for complex SQL

> [!TIP]
> While both `JdbcTemplate` and `JdbcClient` are fully supported, consider using `JdbcClient` for new projects to take advantage of its more modern and readable API.

[Back to Table of Contents](#table-of-contents)

## Using JPA entities and Spring Data JPA

The Java Persistence API (JPA) is a layer over persistence providers like Hibernate. In modern Spring Boot development, most developers skip manual JPA DAO implementations and use Spring Data JPA repositories directly. This exercise demonstrates the practical workflow: create JPA entities with proper annotations, then implement repositories using Spring Data JPA.

### Step 1: Create the JPA Entity

1. We'll use the same `Officer` class from the previous exercise, but add JPA annotations to make it a proper entity. Update the `Officer` class in the `com.kousenit.persistence.entities` package:

```java
@Entity
@Table(name = "officers")
public class Officer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private Rank rank;

    private String firstName;

    private String lastName;

    public Officer() {}

    public Officer(Rank rank, String firstName, String lastName) {
        this.rank = rank;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // ... getters and setters as before ...
}
```

**Key JPA annotations:**
- `@Entity`: Marks this class as a JPA entity
- `@Table(name = "officers")`: Specifies the database table name
- `@Id`: Marks the primary key field
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Auto-generates the ID
- `@Enumerated(EnumType.STRING)`: Stores enum values as strings rather than ordinals

### Step 2: Configure JPA Settings

2. Add JPA configuration to `src/main/resources/application.yml`:

```yaml
spring:
    jpa:
        hibernate:
            ddl-auto: update
        show-sql: true
        properties:
            hibernate.format_sql: true
    h2:
        console:
            enabled: true
```

**Configuration details:**
- `ddl-auto: update`: Creates tables and adds columns without dropping data
- `show-sql: true`: Logs generated SQL queries
- `hibernate.format_sql: true`: Formats SQL for better readability
- `h2.console.enabled: true`: Enables H2 web console

### Step 3: Create Spring Data JPA Repository

3. Spring Data works by defining an interface that extends one of the provided repository interfaces. Create an interface called `OfficerRepository` in the `com.kousenit.persistence.dao` package:

```java
public interface OfficerRepository extends JpaRepository<Officer, Integer> {
    // Spring Data JPA generates all CRUD methods automatically
    
    // Custom query methods using method naming conventions
    List<Officer> findByRank(Rank rank);
    List<Officer> findByLastName(String lastName);
    List<Officer> findByRankAndLastNameLike(Rank rank, String lastName);
    
    // Optional: Custom JPQL queries
    @Query("SELECT o FROM Officer o WHERE o.firstName = :firstName")
    List<Officer> findByFirstNameQuery(@Param("firstName") String firstName);
}
```

**Key points:**
- Extends `JpaRepository<Officer, Integer>` (entity type and ID type)
- Spring Data automatically generates implementations for standard CRUD operations
- Method naming conventions create queries (e.g., `findByRank` becomes `WHERE rank = ?`)
- `@Query` annotation allows custom JPQL queries

### Step 4: Create Repository Tests

4. Create a comprehensive test class `OfficerRepositoryTest` in the `src/test/java` directory:

```java
@SpringBootTest
@Transactional
@TestMethodOrder(OrderAnnotation.class)
class OfficerRepositoryTest {
    
    @Autowired
    private OfficerRepository repository;

    @Test
    @Order(1)
    void testSave() {
        Officer officer = new Officer(Rank.LIEUTENANT, "Nyota", "Uhura");
        officer = repository.save(officer);
        assertNotNull(officer.getId());
    }

    @Test
    @Order(2)
    void findById() {
        Optional<Officer> officer = repository.findById(1);
        assertTrue(officer.isPresent());
        assertEquals("Kirk", officer.get().getLastName());
    }

    @Test
    @Order(3)
    void findAll() {
        List<String> lastNames = repository.findAll().stream()
                .map(Officer::getLastName)
                .collect(Collectors.toList());
        assertThat(lastNames).contains("Kirk", "Picard", "Sisko", "Janeway", "Archer");
    }

    @Test
    @Order(4)
    void count() {
        assertEquals(5, repository.count());
    }

    @Test
    @Order(5)
    void findByRank() {
        List<Officer> captains = repository.findByRank(Rank.CAPTAIN);
        assertEquals(5, captains.size());
        captains.forEach(officer -> assertEquals(Rank.CAPTAIN, officer.getRank()));
    }

    @Test
    @Order(6)
    void findByLastName() {
        List<Officer> kirks = repository.findByLastName("Kirk");
        assertEquals(1, kirks.size());
        assertEquals("James", kirks.get(0).getFirstName());
    }

    @Test
    @Order(7)
    void findByRankAndLastNameLike() {
        List<Officer> officers = repository.findByRankAndLastNameLike(Rank.CAPTAIN, "%a%");
        assertFalse(officers.isEmpty());
        officers.forEach(o -> {
            assertEquals(Rank.CAPTAIN, o.getRank());
            assertTrue(o.getLastName().contains("a"));
        });
    }

    @Test
    @Order(8)
    void existsById() {
        assertTrue(repository.existsById(1));
        assertFalse(repository.existsById(999));
    }

    @Test
    @Order(9)
    void delete() {
        long initialCount = repository.count();
        Optional<Officer> officer = repository.findById(1);
        assertTrue(officer.isPresent());
        
        repository.delete(officer.get());
        assertEquals(initialCount - 1, repository.count());
        assertFalse(repository.existsById(1));
    }
}
```

### Step 5: Test and Explore

5. Run the tests to verify everything works correctly. You should see:
   - Hibernate DDL statements creating the `officers` table
   - SQL queries being logged and formatted
   - All tests passing

6. **H2 Console Access**: Start the application and navigate to `http://localhost:8080/h2-console`:
   - **JDBC URL**: `jdbc:h2:mem:testdb`
   - **Username**: `sa`
   - **Password**: (leave empty)

### Step 6: Add REST Endpoints (Optional)

7. For a complete modern setup, add Spring Data REST to automatically expose your repositories as REST endpoints. Add to `build.gradle`:

```groovy
dependencies {
    // ... existing dependencies ...
    implementation 'org.springframework.boot:spring-boot-starter-data-rest'
    implementation 'org.springframework.data:spring-data-rest-hal-explorer'
}
```

8. After rebuilding, navigate to `http://localhost:8080` to see the HAL Explorer, which provides a web interface for your REST API.

## Key Learning Points

This exercise demonstrates several important concepts:

- **JPA Entity Mapping**: Using annotations to map Java classes to database tables
- **Modern Repository Pattern**: Spring Data JPA eliminates boilerplate DAO code
- **Automatic Query Generation**: Method naming conventions create queries automatically
- **Configuration**: Hibernate and H2 settings for development
- **Testing**: Comprehensive repository testing with Spring Boot Test
- **REST Exposure**: Automatic REST API generation with Spring Data REST

> [!TIP]
> This approach represents modern Spring Boot development: focus on domain modeling with JPA entities, then leverage Spring Data JPA for data access. Manual JPA DAO implementations are rarely needed in practice.

[Back to Table of Contents](#table-of-contents)