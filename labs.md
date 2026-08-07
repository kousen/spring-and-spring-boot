# Spring Boot Labs

This document contains hands-on exercises for learning Spring Boot fundamentals, from basic web applications to database persistence patterns.

## Prerequisites

- **Java 21** (standardized across all projects)
- **Spring Boot 4.1.0** (current version, based on Spring Framework 7)
- **Gradle 9.6.1** (provided by the wrapper)
- IDE with Spring Boot support (IntelliJ IDEA, Spring Tool Suite, or VS Code)

> [!IMPORTANT]
> These labs are designed for Spring Boot 4.1.0 with Java 21. All code examples use modern Java features including records, text blocks, pattern matching, and enhanced switch expressions.

## Table of Contents

1. [Creating a New Project](#creating-a-new-project)
2. [Add a REST Controller](#add-a-rest-controller)
3. [Building a REST Client](#building-a-rest-client)
4. [HTTP Interfaces (Spring Boot 3+)](#http-interfaces-spring-boot-3)
5. [Basic REST API Consumption with HTTP Interfaces](#basic-rest-api-consumption-with-http-interfaces)
6. [Configuration with @Value and Error Handling](#configuration-with-value-and-error-handling)
7. [Using the JDBC Template](#using-the-jdbc-template)
8. [Using the JDBC Client (Spring Boot 3.2+)](#using-the-jdbc-client-spring-boot-32)
9. [Using JPA entities and Spring Data JPA](#using-jpa-entities-and-spring-data-jpa)
10. [Spring Profiles for Environment-Specific Configuration](#spring-profiles-for-environment-specific-configuration)
11. [Optional: Aspect-Oriented Programming (AOP) with Spring](#optional-aspect-oriented-programming-aop-with-spring)
12. [Optional: A Taste of Spring AI](#optional-a-taste-of-spring-ai)

## Creating a New Project

1. Go to http://start.spring.io to access the Spring Initializr
2. Under **Project**, select **Gradle - Groovy** (the default is Maven)
3. Leave the default Spring Boot version (4.1.x) and select **Java 21** in the Project Metadata section
4. Specify the Group as `com.kousenit` and the Artifact as `demo`
5. Add the _Spring Web_ and _Thymeleaf_ dependencies
6. Click the "Generate" button to download a zip file containing the project files
7. Unzip the downloaded "demo.zip" file into any directory you like (but remember where it is)
8. Import the project into your IDE
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

9. As part of the import process, the IDE will download all the required dependencies
10. Open the file `src/main/java/com/kousenit/demo/DemoApplication.java` and note that it contains a standard Java "main" method (with signature: `public static void main(String[] args)`)
11. Start the application by running this method. There won't be any web components available yet, but you can see the start up of the application in the command window.
12. Add a controller by creating a file called `com.kousenit.demo.controllers.HelloController` in the `src/main/java` directory

> [!NOTE]
> The goal is to have the `HelloController` class in the `com.kousenit.demo.controllers` package starting at the root directory `src/main/java`

13. The code for the `HelloController` is:

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

14. Create a file called `welcome.html` in the `src/main/resources/templates` folder
15. The code for the `welcome.html` file is:

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

16. Start up the application and navigate to http://localhost:8080/hello. You should see the string "Hello, World!" in the browser
17. Change the URL in the browser to http://localhost:8080/hello?name=Dolly. You should now see the string "Hello, Dolly!" in the browser
18. Shut down the application (there's no graceful way to do that -- just hit the stop button in your IDE)
19. Add a home page to the app by creating a file called `index.html` in the `src/main/resources/static` folder
20. The code for the `index.html` file is:

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

21. From a command prompt in the root of the project, build the application:

 > gradlew build

22. Now you can start the application with a generated executable jar file:

 > java -jar build/libs/demo-0.0.1-SNAPSHOT.jar

23. Navigate to http://localhost:8080 and see the new home page. From there you can navigate to the greeting page, and manually try adding a `name` parameter to the URL there
24. Again stop the application (use Ctrl-C in the command window)
25. Start it one more time using a special gradle task:

 > gradlew bootRun

26. When again you're happy the app is running properly, shut it down
27. Because the controller is a simple POJO, you can unit test it by simply instantiating the controller and calling its `sayHello` method directly. To do so, add a class called `HelloControllerUnitTest` to the `com.kousenit.demo.controllers` package in the _test_ folder, `src/test/java`
28. The code for the test class is:

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

29. Run the test by executing this class as a JUnit test. It should pass. It's not terribly useful, however, since it isn't affected by the request mapping or the request parameter.
30. To perform an integration test instead, use the `MockMVC` classes available in Spring. Create a new class called `HelloControllerMockMVCTest` in the `com.kousenit.demo.controllers` package in `src/test/java`
31. The code for the integration test is:

```java
package com.kousenit.demo.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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

32. The tests should pass successfully. One of the advantages of the `@WebMvcTest` annotation over the generic `@SpringBootTest` annotation is that it allows you to automatically inject an instance of `MockMvc`, as shown.

## Add a Rest Controller

1. Add another class to the `com.kousenit.demo.controllers` package called `HelloRestController`. This controller will be used to model a RESTful web service, though at this stage it will be limited to HTTP GET requests (for reasons explained below).
2. Add the `@RestController` annotation to the class.
3. By default, REST controllers will serialize and deserialize Java classes into JSON data using the Jackson 2 JSON library, which is currently on the classpath by default. To have an object (other than a trivial `String`) to serialize, add a class called `Greeting` to the `com.kousenit.demo.json` package. In a larger application, this would represent a domain class that you can store in a database or other persistent storage mechanism.
4. Create the `Greeting` class as a **record** (the modern Java approach):

```java
package com.kousenit.demo.json;

public record Greeting(String message) { }
```

> [!NOTE]
> This single-line record automatically provides:
> - Constructor: `new Greeting(String message)`
> - Accessor: `greeting.message()`
> - `equals()`, `hashCode()`, and `toString()` methods
> - Immutability (all fields are final)
>
> Records are perfect for DTOs (Data Transfer Objects) and eliminate ~40 lines of boilerplate!

**For reference** - Traditional class approach (not recommended):

<details>
<summary>Click to see the old verbose approach</summary>

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
</details>

5. Back in the `HelloRestController`, add a method called `greet` that takes a `String` called `name` as an argument and returns a `Greeting`.
6. Annotate the `greet` method with a `@GetMapping` whose argument is `"/rest"`, which means that the URL to access the method will be http://localhost:8080/rest .
7. Add the `@RequestParam` annotation to the argument, with the properties `required` set to `false` and `defaultValue` set to `World`.
8. In the body of the method, return a new instance of `Greeting` whose constructor argument should be `"Hello, " + name + "!"`.
9. The full class looks like (note that the string concatenation uses modern `String.formatted()` method)

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
        return new Greeting("Hello, %s!".formatted(name));
    }
}
```

> [!TIP]
> Modern Java's `String.formatted()` is cleaner than `String.format()`. Both work, but `formatted()` reads better as a method chain.

10. You can now run the application and check the behavior using either `curl` or a similar command-line tool, or simply accessing the URL in a browser, either with or without a name.
11. To create a test for the REST controller, we will use the `RestTestClient` class, introduced in Spring Framework 7. It provides a fluent API for testing HTTP endpoints, built on the same `RestClient` you will use in the next exercise to consume external services. Add a class called `HelloRestControllerIntegrationTest` in the `src/test/java` tree in the same package as the REST controller class.
12. `RestTestClient` support comes from a dedicated Spring Boot module. Add it to the `dependencies` block of your `build.gradle` file:

```groovy
testImplementation 'org.springframework.boot:spring-boot-resttestclient'
```

13. This time, when adding the `@SpringBootTest` annotation, add the argument `webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT` so the test starts a real server on an unused port. Then add the `@AutoConfigureRestTestClient` annotation (from `org.springframework.boot.resttestclient.autoconfigure`), which makes a `RestTestClient` bound to that server available for injection.
14. Add two tests, one for greetings without a name and one for greetings with a name.
15. The tests should look like:

```java
@Test
public void greetWithoutName(@Autowired RestTestClient client) {
    client.get().uri("/rest")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(Greeting.class)
            .isEqualTo(new Greeting("Hello, World!"));
}

@Test
public void greetWithName(@Autowired RestTestClient client) {
    Greeting response = client.get().uri("/rest?name=Dolly")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Greeting.class)
            .returnResult()
            .getResponseBody();
    assert response != null;
    assertEquals("Hello, Dolly!", response.message());
}
```

16. The first test stays entirely inside the fluent API: `exchange()` executes the request, and the `expectStatus`, `expectHeader`, and `expectBody` methods assert on the response. Because `Greeting` is a record, `isEqualTo` can compare the de-serialized body against an expected instance directly.
17. The second test uses `returnResult()` to extract the de-serialized body from the fluent chain, so you can make ordinary JUnit assertions about it. Use whichever style you prefer.
18. The tests should now pass. This application only checks HTTP GET requests so far. The solution project also demonstrates a POST test — see `postGreeting` in `HelloRestControllerIntegrationTest`, which sends a `Greeting` with `body(...)` and expects HTTP 201 CREATED.

## Building a REST Client

This exercise uses the modern `RestClient` class to access RESTful web services. `RestClient` was introduced in Spring 6.1 as the modern replacement for `RestTemplate`, providing a fluent API for synchronous HTTP operations. We'll consume the [Launch Library 2 API](https://ll.thespacedevs.com/) to retrieve information about active space expeditions and astronauts currently in space.

> [!NOTE]
> The Launch Library API provides real-time data about space launches, astronauts, and space stations. It's an excellent API for learning because it has a rich, nested JSON structure that demonstrates real-world data mapping patterns.

### Step 1: Create the Project

1. Create a new Spring Boot project (either by using the Initializr at http://start.spring.io or using your IDE) called `restclient`. Add the _Spring Web_ dependency.

2. The project structure should look like:
   ```
   restclient/
   ├── src/main/java/com/kousenit/restclient/
   │   ├── RestClientApplication.java
   │   ├── json/
   │   └── services/
   └── src/test/java/com/kousenit/restclient/
       └── services/
   ```

### Step 2: Understand the API Response

3. The Launch Library API returns detailed expedition data. Here's a simplified view of the JSON structure from `https://ll.thespacedevs.com/2.3.0/expeditions/?is_active=true&mode=detailed`:

```json
{
  "count": 2,
  "results": [
    {
      "id": 156,
      "name": "Expedition 72",
      "start": "2024-09-23T00:00:00Z",
      "end": null,
      "spacestation": {
        "id": 4,
        "name": "International Space Station",
        "orbit": "Low Earth Orbit"
      },
      "crew": [
        {
          "role": { "role": "Commander" },
          "astronaut": {
            "id": 477,
            "name": "Suni Williams",
            "agency": { "name": "National Aeronautics and Space Administration", "abbrev": "NASA" },
            "nationality": [{ "name": "United States of America", "nationality_name": "American" }],
            "time_in_space": "P322DT18H26M",
            "bio": "..."
          }
        }
      ]
    }
  ]
}
```

> [!TIP]
> Real-world APIs often have deeply nested structures. Records handle this elegantly by composing smaller records into larger ones.

### Step 3: Create Record Classes for JSON Mapping

4. Create a class called `LaunchLibraryRecords` in the `com.kousenit.restclient.json` package. We'll use nested records to model the entire response structure:

```java
package com.kousenit.restclient.json;

import java.util.List;

public class LaunchLibraryRecords {
    // Root response wrapper
    public record ExpeditionResponse(
            int count,
            List<Expedition> results
    ) {}

    // Expedition with space station and crew
    public record Expedition(
            int id,
            String name,
            String start,
            String end,
            SpaceStation spacestation,
            List<CrewMember> crew
    ) {}

    // Space station basics
    public record SpaceStation(
            int id,
            String name,
            String orbit
    ) {}

    // Crew assignment (role + astronaut)
    public record CrewMember(
            Role role,
            Astronaut astronaut
    ) {}

    public record Role(
            String role
    ) {}

    // Astronaut details
    public record Astronaut(
            int id,
            String name,
            Agency agency,
            List<Nationality> nationality,
            String time_in_space,
            String bio
    ) {}

    public record Agency(
            String name,
            String abbrev
    ) {}

    public record Nationality(
            String name,
            String nationality_name
    ) {}

    // Flattened view for easier consumption
    public record AstronautAssignment(
            String astronautName,
            String role,
            String agency,
            String stationName
    ) {}
}
```

> [!NOTE]
> Grouping related records in a single file with a container class keeps all the API types together. The static import `import static ...LaunchLibraryRecords.*` lets you use the record names directly without prefixes.

> [!TIP]
> You don't need to map every field in the JSON response. Jackson (Spring's default JSON library) will ignore unmapped fields. Only include the fields your application actually needs.

### Step 4: Create the Service Class

5. Create a service class called `LaunchLibraryService` in the `com.kousenit.restclient.services` package:

```java
package com.kousenit.restclient.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.kousenit.restclient.json.LaunchLibraryRecords.*;

@Service
public class LaunchLibraryService {
    private static final String BASE_URL = "https://ll.thespacedevs.com";
    private final RestClient client;

    public LaunchLibraryService(RestClient.Builder builder) {
        this.client = builder.baseUrl(BASE_URL).build();
    }

    public List<Expedition> getExpeditions() {
        return Objects.requireNonNull(client.get()
                        .uri("/2.3.0/expeditions/?is_active=true&mode=detailed")
                        .retrieve()
                        .body(ExpeditionResponse.class))
                .results();
    }
}
```

> [!IMPORTANT]
> Notice we inject `RestClient.Builder` rather than creating the `RestClient` directly. Spring Boot auto-configures a `RestClient.Builder` bean that includes any application-wide settings (timeouts, interceptors, etc.). This is the recommended approach for production code.

### Step 5: Add Business Methods

6. Real services typically transform raw API data into formats useful to callers. Add methods to provide flattened views of the data:

```java
public List<AstronautAssignment> getAstronautAssignments() {
    return getExpeditions().stream()
            .flatMap(expedition -> expedition.crew().stream()
                    .map(member -> new AstronautAssignment(
                            member.astronaut().name(),
                            member.role().role(),
                            member.astronaut().agency().abbrev(),
                            expedition.spacestation().name()
                    )))
            .toList();
}

public Map<String, Long> getCrewCountByStation() {
    return getExpeditions().stream()
            .collect(Collectors.groupingBy(
                    exp -> exp.spacestation().name(),
                    Collectors.summingLong(exp -> exp.crew().size())
            ));
}
```

> [!TIP]
> The `flatMap` operation is key here—it "flattens" the nested structure (expeditions → crew members) into a single stream of astronaut assignments. This is a common pattern when working with nested API responses.

### Step 6: Write Tests

7. Create a test class called `LaunchLibraryServiceTest` in the `com.kousenit.restclient.services` package under `src/test/java`:

```java
package com.kousenit.restclient.services;

import com.kousenit.restclient.json.LaunchLibraryRecords.AstronautAssignment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LaunchLibraryServiceTest {
    @Autowired
    private LaunchLibraryService service;

    @Test
    void expeditions_have_crew_aboard_stations() {
        var expeditions = service.getExpeditions();

        assertThat(expeditions).isNotEmpty();
        assertThat(expeditions).allSatisfy(expedition -> {
            assertThat(expedition.spacestation()).isNotNull();
            assertThat(expedition.spacestation().name()).isNotBlank();
            assertThat(expedition.crew()).isNotEmpty();
        });
    }

    @Test
    void astronaut_assignments_have_required_fields() {
        List<AstronautAssignment> assignments = service.getAstronautAssignments();

        assertThat(assignments).isNotEmpty();
        assertThat(assignments).allSatisfy(assignment -> {
            assertThat(assignment.astronautName()).isNotBlank();
            assertThat(assignment.role()).isNotBlank();
            assertThat(assignment.agency()).isNotBlank();
            assertThat(assignment.stationName()).isNotBlank();
        });
    }

    @Test
    void crew_count_by_station_returns_positive_counts() {
        Map<String, Long> crewCounts = service.getCrewCountByStation();

        assertThat(crewCounts).isNotEmpty();
        assertThat(crewCounts.values()).allSatisfy(count ->
                assertThat(count).isPositive()
        );
    }
}
```

> [!NOTE]
> These tests verify the *shape* of the data rather than specific values. This makes them resilient to real-world changes (astronauts come and go, expeditions change) while still catching deserialization issues or API changes.

> [!TIP]
> AssertJ's `allSatisfy()` is perfect for validating collections from external APIs. Each element is checked against the lambda, and failures clearly report which element failed and why.

8. Run the tests and verify they pass:

```bash
./gradlew test --tests LaunchLibraryServiceTest
```

### Bonus: A POST Request with a Binary Response (Text to Speech)

*Optional — requires an OpenAI API key in the `OPENAI_API_KEY` environment variable.*

So far every call has been a GET returning JSON. The `TextToSpeechService` in the solution project rounds out the picture with a `RestClient` POST that sends a JSON body, authenticates with a bearer token, and receives **binary** data — an mp3 of the spoken text:

```java
@Service
public class TextToSpeechService {
    private final RestClient client;

    public TextToSpeechService(RestClient.Builder builder,
                               @Value("${OPENAI_API_KEY:}") String apiKey) {
        client = builder.baseUrl("https://api.openai.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public record TtsRequest(String model, String input, String voice) {
    }

    public Path speak(String text, Path outputFile) {
        return client.post()
                .uri("/v1/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TtsRequest("gpt-4o-mini-tts", text, "alloy"))
                .exchange((request, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException(
                                "TTS request failed with status " + response.getStatusCode());
                    }
                    try (var body = response.getBody()) {
                        Files.copy(body, outputFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return outputFile;
                });
    }
}
```

Points to notice:

- A Java record (`TtsRequest`) is serialized to the JSON request body — the same records idiom you used for responses, in the other direction.
- The `exchange(...)` callback exposes the raw response body as an `InputStream`, so `Files.copy` streams the audio straight into a file. If you have used Java's own `HttpClient`, this is the `RestClient` analog of `HttpResponse.BodyHandlers.ofFile(path)` — the mp3 is never buffered in memory.
- The service works well beyond English — the tests generate both Spanish (properly accented) and Hindi audio, the latter passing Devanagari script straight through:

```java
service.speak("नमस्ते! स्प्रिंग बूट कोर्स में आपका स्वागत है। खूब मज़ा कीजिए!",
        Path.of("build", "swagat.mp3"));
```

  Run `TextToSpeechServiceTest` and play the mp3 files it writes to the `build` directory. (The tests skip themselves when no API key is set.)

[Back to Table of Contents](#table-of-contents)

### Bonus: One Key, Hundreds of Models (OpenRouter)

*Optional — requires an OpenRouter API key in the `OPENROUTER_API_KEY` environment variable. Sign up at [openrouter.ai](https://openrouter.ai); the demo uses only free models, so no credits are needed.*

[OpenRouter](https://openrouter.ai) is a gateway that fronts hundreds of AI models — from OpenAI, Google, Anthropic, Meta, DeepSeek, Qwen, and many others — behind a single API and a single key. It deliberately mirrors OpenAI's chat completions wire format, which makes it a perfect payoff for this lab: the `RestClient` + records approach you have been using all along retargets to a whole catalog of models by changing nothing but the base URL and a model string.

First, records for the request and response. Note that the response records declare only the fields we actually use — Spring Boot configures Jackson to ignore unknown properties, so the rest of OpenRouter's payload (token usage, timestamps, and so on) is simply skipped:

```java
public class OpenRouterRecords {
    // Request body for the chat completions endpoint (OpenAI-compatible format)
    public record ChatRequest(
            String model,
            List<Message> messages
    ) {}

    public record Message(
            String role,
            String content
    ) {}

    // Response: only the fields we care about — Spring Boot's Jackson
    // configuration ignores the rest of the payload (usage, timestamps, etc.)
    public record ChatResponse(
            String id,
            String model,
            List<Choice> choices
    ) {}

    public record Choice(
            Message message
    ) {}
}
```

The service follows the same skeleton as `TextToSpeechService` — base URL plus bearer token — but this time the response is plain JSON, so the simple `retrieve().body(...)` form is all we need:

```java
@Service
public class OpenRouterService {
    private final Logger logger = LoggerFactory.getLogger(OpenRouterService.class);

    private final RestClient client;
    private final String defaultModel;

    public OpenRouterService(RestClient.Builder builder,
                             @Value("${OPENROUTER_API_KEY:}") String apiKey,
                             @Value("${openrouter.model:openrouter/free}") String defaultModel) {
        client = builder.baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.defaultModel = defaultModel;
    }

    public String chat(String prompt) {
        return chat(defaultModel, prompt);
    }

    public String chat(String model, String prompt) {
        ChatResponse response = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ChatRequest(model, List.of(new Message("user", prompt))))
                .retrieve()
                .body(ChatResponse.class);
        if (response == null || response.choices().isEmpty()) {
            throw new IllegalStateException("No response from OpenRouter for model " + model);
        }
        logger.info("Requested model {}, served by {}", model, response.model());
        return response.choices().getFirst().message().content();
    }
}
```

The default model comes from `application.properties`:

```properties
# OpenRouterService: one OPENROUTER_API_KEY works with hundreds of models.
# "openrouter/free" routes each request to a currently available free model.
openrouter.model=openrouter/free
```

`openrouter/free` is not a model but a **router**: OpenRouter picks one of the currently available free models for each request, and the response's `model` field reports which one actually answered. That is why the service logs both the requested and the served model — run the test twice and you may see two different models reply.

Points to notice:

- The only OpenRouter-specific parts of the service are the base URL and the model string. Because OpenRouter speaks OpenAI's chat completions format, your hand-rolled client needs no vendor SDK — a new provider is just a new constructor argument.
- The overloaded `chat(model, prompt)` method lets you target any specific model in the catalog. The test uses `openai/gpt-oss-20b:free` — OpenAI's open-weights model, served at no charge — but swapping in a DeepSeek, Qwen, or Gemini identifier from [openrouter.ai/models](https://openrouter.ai/models) is a one-string change.
- Free models are rate-limited and the free pool changes over time. If the explicit-model test ever fails with an unknown-model error, pick a current `:free` model from the catalog — the code does not change.

Run `OpenRouterServiceTest` and watch the log to see which model answered. (Like the TTS tests, it skips itself when no API key is set.)

[Back to Table of Contents](#table-of-contents)

## HTTP Interfaces (Spring Boot 3+)

Spring Boot 3.0 introduced HTTP Interfaces, a declarative way to access external RESTful web services. Instead of writing `RestClient` calls manually, you declare an interface with annotated methods, and Spring implements it for you.

> [!NOTE]
> HTTP Interfaces work similarly to Spring Data repositories—you define the contract, Spring provides the implementation.

### Step 1: Create the Interface

1. Add an interface called `LaunchLibraryInterface` to the `services` package:

```java
package com.kousenit.restclient.services;

import org.springframework.web.service.annotation.GetExchange;

import static com.kousenit.restclient.json.LaunchLibraryRecords.*;

public interface LaunchLibraryInterface {
    @GetExchange("/2.3.0/expeditions/?is_active=true&mode=detailed")
    ExpeditionResponse getActiveExpeditions();
}
```

2. The `@GetExchange` annotation marks this method as an HTTP GET request. Spring will implement this interface at runtime, making the HTTP call and deserializing the response automatically.

> [!TIP]
> For other HTTP methods, use `@PostExchange`, `@PutExchange`, `@DeleteExchange`, etc. Parameters can be added using `@PathVariable`, `@RequestParam`, and `@RequestBody` annotations.

### Step 2: Create the Proxy Factory Bean

3. We need to tell Spring how to create an implementation of our interface. Add this bean to your `RestClientApplication` class (or a separate `@Configuration` class):

```java
@Bean
public LaunchLibraryInterface launchLibraryInterface(RestClient.Builder builder) {
    RestClient client = builder
            .baseUrl("https://ll.thespacedevs.com")
            .build();
    RestClientAdapter adapter = RestClientAdapter.create(client);
    HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(adapter)
            .build();
    return factory.createClient(LaunchLibraryInterface.class);
}
```

4. You'll need these imports:

```java
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
```

> [!NOTE]
> The `HttpServiceProxyFactory` creates a dynamic proxy that implements `LaunchLibraryInterface`. Each method call is translated into an HTTP request based on the annotations.

### Step 3: Test the Interface

5. Add a test to `LaunchLibraryServiceTest` (or create a new test class):

```java
@Test
void interface_returns_active_expeditions(@Autowired LaunchLibraryInterface launchLibraryInterface) {
    ExpeditionResponse response = launchLibraryInterface.getActiveExpeditions();

    assertThat(response).isNotNull();
    assertThat(response.count()).isPositive();
    assertThat(response.results()).isNotEmpty();
    assertThat(response.results()).allSatisfy(expedition -> {
        assertThat(expedition.name()).isNotBlank();
        assertThat(expedition.spacestation()).isNotNull();
        assertThat(expedition.crew()).isNotEmpty();
    });
}
```

6. You'll need this additional import in the test:

```java
import static com.kousenit.restclient.json.LaunchLibraryRecords.*;
```

7. Run the test to verify the HTTP Interface works correctly.

> [!TIP]
> HTTP Interfaces are especially powerful when you have many endpoints to consume. The declarative style keeps your code clean and focused on the API contract rather than HTTP mechanics.

## Basic REST API Consumption with HTTP Interfaces

This exercise applies the HTTP Interface pattern you just learned to consume the JSON Placeholder API (https://jsonplaceholder.typicode.com/), a free testing service perfect for practicing declarative REST clients.

> [!NOTE]
> This lab builds directly on the HTTP Interfaces pattern from the previous exercise. We'll use `@GetExchange`, `@PostExchange`, and other declarative annotations instead of manual `RestClient` calls.

### Step 1: Create Domain Classes

1. We'll add to the existing `restclient` project. First, examine the JSON structure from `https://jsonplaceholder.typicode.com/users/1`:

   ```json
   {
     "id": 1,
     "name": "Leanne Graham",
     "username": "Bret",
     "email": "Sincere@april.biz"
   }
   ```

2. Create a simple record for users in `com.kousenit.restclient.json`:

   ```java
   public record SimpleUser(
       Long id,
       String name,
       String username,
       String email
   ) {}
   ```

3. For posts, create another record:

   ```java
   public record Post(
       Long userId,
       Long id,
       String title,
       String body
   ) {}
   ```

### Step 2: Create HTTP Interface

4. Create `JsonPlaceholderInterface` in `com.kousenit.restclient.services`:

   ```java
   public interface JsonPlaceholderInterface {

       @GetExchange("/users")
       List<SimpleUser> getAllUsers();

       @GetExchange("/users/{id}")
       SimpleUser getUserById(@PathVariable Long id);

       @GetExchange("/users/{userId}/posts")
       List<Post> getPostsByUserId(@PathVariable Long userId);

       @PostExchange("/posts")
       Post createPost(@RequestBody Post post);

       @GetExchange("/posts")
       List<Post> getAllPosts();
   }
   ```

   > [!NOTE]
   > Notice how much cleaner this is compared to manual `RestClient` calls. Spring generates all the implementation code for you based on these annotations.

### Step 3: Create Proxy Factory Bean

5. Add the proxy factory bean to your `RestClientApplication` class (or create a separate `@Configuration` class):

   ```java
   @Bean
   public JsonPlaceholderInterface jsonPlaceholderInterface() {
       RestClient client = RestClient.builder()
               .baseUrl("https://jsonplaceholder.typicode.com")
               .build();
       RestClientAdapter adapter = RestClientAdapter.create(client);
       HttpServiceProxyFactory factory = HttpServiceProxyFactory
               .builderFor(adapter)
               .build();
       return factory.createClient(JsonPlaceholderInterface.class);
   }
   ```

### Step 4: Create Tests

6. Create `JsonPlaceholderInterfaceTest`:

   ```java
   @SpringBootTest
   class JsonPlaceholderInterfaceTest {
       private final Logger logger = LoggerFactory.getLogger(JsonPlaceholderInterfaceTest.class);

       @Autowired
       private JsonPlaceholderInterface jsonPlaceholder;

       @Test
       void getAllUsers() {
           List<SimpleUser> users = jsonPlaceholder.getAllUsers();

           assertNotNull(users);
           assertEquals(10, users.size());

           SimpleUser firstUser = users.get(0);
           assertEquals("Leanne Graham", firstUser.name());
           logger.info("Retrieved {} users", users.size());
       }

       @Test
       void getUserById() {
           SimpleUser user = jsonPlaceholder.getUserById(1L);

           assertNotNull(user);
           assertEquals("Leanne Graham", user.name());
           assertEquals("Bret", user.username());
       }

       @Test
       void getPostsByUserId() {
           List<Post> posts = jsonPlaceholder.getPostsByUserId(1L);

           assertNotNull(posts);
           assertEquals(10, posts.size());
           posts.forEach(post -> assertEquals(1L, post.userId()));
       }

       @Test
       void createPost() {
           Post newPost = new Post(1L, null, "Test Title", "Test Body");
           Post created = jsonPlaceholder.createPost(newPost);

           assertNotNull(created.id());
           assertEquals("Test Title", created.title());
           assertEquals("Test Body", created.body());
       }

       @Test
       void getAllPosts() {
           List<Post> posts = jsonPlaceholder.getAllPosts();

           assertNotNull(posts);
           assertEquals(100, posts.size());
           logger.info("Retrieved {} posts", posts.size());
       }
   }
   ```

### Step 5: Optional - Add Error Handling

7. For production use, wrap the interface with a service that handles errors:

   ```java
   @Service
   public class JsonPlaceholderService {
       private final JsonPlaceholderInterface client;
       private final Logger logger = LoggerFactory.getLogger(JsonPlaceholderService.class);

       public JsonPlaceholderService(JsonPlaceholderInterface client) {
           this.client = client;
       }

       public Optional<SimpleUser> getUserByIdSafe(Long id) {
           try {
               return Optional.ofNullable(client.getUserById(id));
           } catch (HttpClientErrorException.NotFound e) {
               logger.warn("User {} not found", id);
               return Optional.empty();
           } catch (Exception e) {
               logger.error("Error fetching user {}", id, e);
               throw new ServiceUnavailableException("Unable to fetch user", e);
           }
       }
   }
   ```

### Key Learning Points

- **Declarative API Clients**: Define interfaces instead of writing implementation code
- **HTTP Exchange Annotations**: `@GetExchange`, `@PostExchange`, `@PutExchange`, `@DeleteExchange`
- **Path Variables**: Use `@PathVariable` for URL parameters
- **Request Bodies**: Use `@RequestBody` for POST/PUT payloads
- **Type Safety**: Collections work automatically without `ParameterizedTypeReference`
- **Less Boilerplate**: Spring generates all the HTTP client code
- **Consistent Pattern**: Same approach works for any REST API

> [!TIP]
> HTTP Interfaces are ideal for well-defined external APIs. The declarative approach makes your code more maintainable and easier to test with mocks.

### Comparison: Before and After

**Before (Manual RestClient):**
```java
public List<SimpleUser> getAllUsers() {
    return restClient.get()
            .uri("/users")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(new ParameterizedTypeReference<List<SimpleUser>>() {});
}
```

**After (HTTP Interface):**
```java
@GetExchange("/users")
List<SimpleUser> getAllUsers();
```

The declarative approach is:
- **Shorter**: One line vs. six
- **Clearer**: Intent is obvious from the annotation
- **Type-safe**: No need for `ParameterizedTypeReference`
- **Testable**: Easy to mock the interface

[Back to Table of Contents](#table-of-contents)

## Configuration with @Value and Error Handling

This exercise builds on the basic REST client by adding configuration management with `@Value` annotations and proper error handling strategies.

> [!NOTE]
> The `@Value` annotation allows you to inject configuration from multiple sources: application properties, system properties, and environment variables.

### Step 1: Add Configuration Properties

1. Add to `src/main/resources/application.properties`:

   ```properties
   # API Configuration
   api.jsonplaceholder.base-url=https://jsonplaceholder.typicode.com
   api.jsonplaceholder.timeout=5000
   api.jsonplaceholder.max-retries=3

   # Application Info
   app.name=REST Client Demo
   app.version=1.0.0
   ```

### Step 2: Create Configuration Class

2. Create `ApiConfig` in `com.kousenit.restclient.config`:

   ```java
   @Configuration
   public class ApiConfig {
       
       @Value("${app.name}")
       private String applicationName;
       
       @Value("${app.version}")
       private String applicationVersion;
       
       // System property with default
       @Value("${java.version:unknown}")
       private String javaVersion;
       
       // Environment variable
       @Value("${USER:anonymous}")
       private String userName;
       
       public String getApplicationInfo() {
           return String.format("%s v%s (Java %s, User: %s)", 
                   applicationName, applicationVersion, javaVersion, userName);
       }
       
       // Getters...
   }
   ```

### Step 3: Enhanced Service with Configuration

3. Create `ConfigurableJsonPlaceholderService`:

   ```java
   @Service
   public class ConfigurableJsonPlaceholderService {
       private final RestClient restClient;
       private final String baseUrl;
       private final int timeoutMs;
       private final int maxRetries;
       private final Logger logger = LoggerFactory.getLogger(ConfigurableJsonPlaceholderService.class);

       public ConfigurableJsonPlaceholderService(
               @Value("${api.jsonplaceholder.base-url}") String baseUrl,
               @Value("${api.jsonplaceholder.timeout:5000}") int timeoutMs,
               @Value("${api.jsonplaceholder.max-retries:3}") int maxRetries) {
           
           this.baseUrl = baseUrl;
           this.timeoutMs = timeoutMs;
           this.maxRetries = maxRetries;
           
           // Configure RestClient with timeout
           this.restClient = RestClient.builder()
                   .baseUrl(baseUrl)
                   .requestFactory(clientRequestFactory())
                   .build();
           
           logger.info("Initialized with baseUrl: {}, timeout: {}ms, maxRetries: {}", 
                      baseUrl, timeoutMs, maxRetries);
       }

       private ClientHttpRequestFactory clientRequestFactory() {
           var factory = new SimpleClientHttpRequestFactory();
           factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
           factory.setReadTimeout(Duration.ofMillis(timeoutMs));
           return factory;
       }

       // Enhanced method with retry logic
       public Optional<SimpleUser> getUserByIdWithRetry(Long id) {
           int attempts = 0;
           Exception lastException = null;
           
           while (attempts < maxRetries) {
               try {
                   logger.debug("Attempt {} to fetch user {}", attempts + 1, id);
                   SimpleUser user = restClient.get()
                           .uri("/users/{id}", id)
                           .retrieve()
                           .body(SimpleUser.class);
                   return Optional.ofNullable(user);
               } catch (HttpClientErrorException.NotFound e) {
                   logger.warn("User {} not found", id);
                   return Optional.empty();
               } catch (Exception e) {
                   lastException = e;
                   attempts++;
                   if (attempts < maxRetries) {
                       logger.warn("Attempt {} failed, retrying...", attempts);
                       try {
                           Thread.sleep(1000 * attempts); // Exponential backoff
                       } catch (InterruptedException ie) {
                           Thread.currentThread().interrupt();
                           break;
                       }
                   }
               }
           }
           
           logger.error("Failed after {} attempts", maxRetries, lastException);
           throw new ServiceUnavailableException("Service unavailable after " + maxRetries + " attempts");
       }

       // Custom exception
       public static class ServiceUnavailableException extends RuntimeException {
           public ServiceUnavailableException(String message) {
               super(message);
           }
       }
   }
   ```

### Step 4: Add Nested JSON Support

4. Create records for complex JSON structures:

   ```java
   public record Address(
       String street,
       String suite,
       String city,
       String zipcode
   ) {}

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

### Step 5: Test Configuration and Error Handling

5. Create comprehensive tests:

   ```java
   @SpringBootTest
   class ConfigurableJsonPlaceholderServiceTest {
       
       @Autowired
       private ConfigurableJsonPlaceholderService service;
       
       @Autowired
       private ApiConfig apiConfig;
       
       @Test
       void testConfiguration() {
           assertNotNull(service.getBaseUrl());
           assertEquals(5000, service.getTimeoutMs());
           assertEquals(3, service.getMaxRetries());
       }
       
       @Test
       void testRetryLogic() {
           // Test with valid ID
           Optional<SimpleUser> user = service.getUserByIdWithRetry(1L);
           assertTrue(user.isPresent());
       }
       
       @Test
       void testNotFound() {
           // Test 404 handling
           Optional<SimpleUser> user = service.getUserByIdWithRetry(999L);
           assertFalse(user.isPresent());
       }
       
       @Test
       void testApiConfig() {
           String appInfo = apiConfig.getApplicationInfo();
           assertNotNull(appInfo);
           assertTrue(appInfo.contains("REST Client Demo"));
           assertTrue(appInfo.contains("1.0.0"));
       }
   }
   ```

### Step 6: Type-Safe Configuration with @ConfigurationProperties

`@Value` works well for a handful of individual properties, but Spring Boot's preferred approach for a *group* of related properties is `@ConfigurationProperties`, which binds a whole prefix onto one object. The solution project already uses it — look at `MyProperties`:

```java
@ConfigurationProperties("my.service")
public class MyProperties {
    private String jokeUrl;

    public String getJokeUrl() {
        return jokeUrl;
    }

    public void setJokeUrl(String jokeUrl) {
        this.jokeUrl = jokeUrl;
    }
}
```

with this line in `application.properties`:

```properties
my.service.joke-url=https://api.chucknorris.io
```

and this annotation on the application class to register it:

```java
@SpringBootApplication
@EnableConfigurationProperties(MyProperties.class)
public class RestclientApplication { ... }
```

Note the relaxed binding: the kebab-case property `joke-url` binds to the camel-case field `jokeUrl` automatically. `JokeService` then injects `MyProperties` like any other bean and calls `getJokeUrl()` — no `@Value` expressions, and the property names are checked in one place instead of being scattered through the code as strings.

**When to use which:** `@Value` for one-off values; `@ConfigurationProperties` when several properties share a prefix, when you want IDE auto-completion for them (via the configuration metadata), or when they deserve validation annotations.

### Key Learning Points

- **@Value Patterns**: Property injection with defaults
- **@ConfigurationProperties**: Type-safe binding of a property prefix onto an object, with relaxed binding (kebab-case to camelCase)
- **Constructor Injection**: Immutable configuration
- **Timeout Configuration**: Connection and read timeouts
- **Retry Logic**: Exponential backoff for resilience
- **Error Differentiation**: Handling 404 vs other errors
- **Custom Exceptions**: Domain-specific error types
- **Logging Best Practices**: Debug, warn, and error levels

> [!TIP]
> Always provide sensible defaults for configuration values. This makes your application more resilient and easier to deploy.

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

Spring Framework 6.1 (included in Spring Boot 3.2+) introduced `JdbcClient`, a modern fluent API that serves as a more user-friendly alternative to `JdbcTemplate`. While `JdbcTemplate` remains widely used and fully supported, `JdbcClient` provides a cleaner, more readable approach that aligns with other modern Spring APIs like `RestClient`.

> [!NOTE]
> This exercise uses the same database schema and `Officer` entity from the previous JdbcTemplate lab. You can continue with the same `persistence` project or create a new one following the same setup steps.

1. Create a new DAO implementation called `JdbcClientOfficerDAO` that implements the same `OfficerDAO` interface, but uses `JdbcClient` instead of `JdbcTemplate`.

2. Add the `@Repository` annotation and inject both `DataSource` and `JdbcClient` into the constructor. For insert operations, we'll use `SimpleJdbcInsert`:

   ```java
   @Repository
   public class JdbcClientOfficerDAO implements OfficerDAO {
       private final JdbcClient jdbcClient;
       private final SimpleJdbcInsert insertOfficer;

       @Autowired
       public JdbcClientOfficerDAO(DataSource dataSource, JdbcClient jdbcClient) {
           this.jdbcClient = jdbcClient;
           this.insertOfficer = new SimpleJdbcInsert(dataSource)
                   .withTableName("officers")
                   .usingGeneratedKeyColumns("id");
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

6. For the finder methods, `JdbcClient` provides automatic object mapping. Implement `findById`:

   ```java
   @Override
   public Optional<Officer> findById(Integer id) {
       return jdbcClient.sql("SELECT * FROM officers WHERE id = :id")
               .param("id", id)
               .query(Officer.class)
               .optional();
   }
   ```

7. Implement `findAll` using automatic mapping:

   ```java
   @Override
   public List<Officer> findAll() {
       return jdbcClient.sql("SELECT * FROM officers")
               .query(Officer.class)
               .list();
   }
   ```

8. For the `save` method, we'll use `SimpleJdbcInsert` for clean insert operations:

   ```java
   @Override
   public Officer save(Officer officer) {
       if (officer.getId() == null) {
           // Insert new officer using SimpleJdbcInsert
           Map<String, Object> parameters = Map.of(
               "rank", officer.getRank().name(),
               "first_name", officer.getFirstName(),
               "last_name", officer.getLastName()
           );
           var newId = insertOfficer.executeAndReturnKey(parameters).intValue();
           return new Officer(newId, officer.getRank(), 
                   officer.getFirstName(), officer.getLastName());
       } else {
           // Update existing officer using JdbcClient
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
   > For even cleaner code, you can use `BeanPropertySqlParameterSource` which uses reflection to extract properties from the object:
   > 
   > ```java
   > // Alternative approach using reflection
   > var paramSource = new BeanPropertySqlParameterSource(officer);
   > var newId = insertOfficer.executeAndReturnKey(paramSource).intValue();
   > ```

   > [!TIP]
   > Notice how `JdbcClient` allows us to use text blocks for multi-line SQL, making the code more readable. The named parameters (`:paramName`) are much cleaner than positional parameters (`?`).

9. Add the necessary imports to your class:

   ```java
   import org.springframework.jdbc.core.simple.JdbcClient;
   import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
   import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
   import javax.sql.DataSource;
   import java.util.Map;
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
- **Automatic Object Mapping**: No need for manual row mappers - simply use `.query(YourClass.class)`
- **Built-in Optional Support**: Methods like `optional()` and `single()` provide better null handling
- **SimpleJdbcInsert Integration**: Clean insert operations with generated key handling
- **Bean Property Mapping**: Automatic parameter extraction using reflection
- **Consistent Design**: Follows the same patterns as other modern Spring clients
- **Text Block Friendly**: Works seamlessly with text blocks for complex SQL

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

## Spring Profiles for Environment-Specific Configuration

Spring profiles provide a powerful way to configure your application differently for various environments (development, testing, production). This exercise demonstrates how to use profiles to switch between different database configurations and environment-specific settings.

> [!NOTE]
> This exercise builds on the persistence project and shows how to configure different databases for different environments using Spring profiles and optionally Docker with Testcontainers.

### Overview of Spring Profiles

Spring profiles allow you to:
- **Segregate configuration** for different environments
- **Conditionally create beans** based on active profiles  
- **Override properties** per environment
- **Group related configuration** together

### Step 1: Update Dependencies

First, add Testcontainers support to `build.gradle` for advanced database testing:

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-data-rest'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.data:spring-data-rest-hal-explorer'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    // Database drivers
    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'org.postgresql:postgresql'
    
    // Testing dependencies (optional - requires Docker)
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test'
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:testcontainers-junit-jupiter'
    testImplementation 'org.testcontainers:testcontainers-postgresql'
    testRuntimeOnly('org.junit.platform:junit-platform-launcher')
}
```

### Step 2: Configure Profile-Specific Properties

Create profile-specific configuration files:

**1. Update `application.yml` (base configuration):**

```yaml
spring:
  profiles:
    active: dev  # Default to dev profile
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate.format_sql: true
    show-sql: true
  h2:
    console:
      enabled: true

logging:
  level:
    sql: debug
    org.springframework.boot.autoconfigure.jdbc: debug

# Application information
app:
  name: Spring Data JPA Persistence Demo
  environment: ${spring.profiles.active:default}
  description: "Spring Data JPA Persistence Demo application"
```

**2. Create `application-dev.yml` (development profile):**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:devdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
  h2:
    console:
      enabled: true
      path: /h2-console

logging:
  level:
    com.kousenit.persistence: debug
    org.hibernate.SQL: debug

# Development specific settings
app:
  description: "Development environment with H2 in-memory database"
  features:
    h2-console: true
    sql-logging: true
```

**3. Create `application-test.yml` (testing profile):**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false  # Reduce noise in tests
  h2:
    console:
      enabled: false  # Not needed in tests

logging:
  level:
    com.kousenit.persistence: info
    org.hibernate.SQL: warn

# Test specific settings
app:
  description: "Test environment with H2 in-memory database"
  features:
    h2-console: false
    sql-logging: false
```

**4. Create `application-prod.yml` (production profile):**

```yaml
spring:
  datasource:
    # These will be overridden by Testcontainers in tests
    # In real production, these would come from environment variables
    url: jdbc:postgresql://localhost:5432/officers_db
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: create-drop  # For testing with Testcontainers
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false
    properties:
      hibernate.jdbc.lob.non_contextual_creation: true
  h2:
    console:
      enabled: false  # No H2 console in production

logging:
  level:
    com.kousenit.persistence: info
    org.hibernate.SQL: warn

# Production specific settings
app:
  description: "Production environment with PostgreSQL database"
  features:
    h2-console: false
    sql-logging: false
    connection-pooling: true
```

### Step 3: Create Profile-Aware Configuration Class

Create a configuration class that demonstrates `@Profile` annotation usage:

```java
@Configuration
public class ProfileConfig {
    private static final Logger logger = LoggerFactory.getLogger(ProfileConfig.class);

    @Value("${app.name}")
    private String appName;

    @Value("${app.environment}")
    private String environment;

    @Value("${app.description}")
    private String description;

    /**
     * Bean that's only active in development profile
     */
    @Bean
    @Profile("dev")
    public DatabaseInfo developmentDatabaseInfo() {
        logger.info("Creating development database info bean");
        return new DatabaseInfo("H2", "In-Memory", "Development", true);
    }

    /**
     * Bean that's only active in test profile
     */
    @Bean
    @Profile("test")
    public DatabaseInfo testDatabaseInfo() {
        logger.info("Creating test database info bean");
        return new DatabaseInfo("H2", "In-Memory", "Testing", false);
    }

    /**
     * Bean that's only active in production profile
     */
    @Bean
    @Profile("prod")
    public DatabaseInfo productionDatabaseInfo() {
        logger.info("Creating production database info bean");
        return new DatabaseInfo("PostgreSQL", "Docker Container", "Production", false);
    }

    /**
     * Bean that's active in development OR test profiles
     */
    @Bean
    @Profile({"dev", "test"})
    public FeatureToggle h2ConsoleFeature() {
        logger.info("Enabling H2 console feature for dev/test profiles");
        return new FeatureToggle("h2-console", true);
    }

    /**
     * Bean that's NOT active in production (using ! prefix)
     */
    @Bean
    @Profile("!prod")
    public FeatureToggle debugFeature() {
        logger.info("Enabling debug features for non-production environments");
        return new FeatureToggle("debug-logging", true);
    }

    // Records for profile-specific configuration
    public record DatabaseInfo(String type, String location, String purpose, boolean consoleEnabled) {}

    public record FeatureToggle(String name, boolean enabled) {}

    public record ApplicationInfo(String name, String environment, String description) {}
}
```

**Key `@Profile` patterns:**
- `@Profile("dev")`: Only active when 'dev' profile is active
- `@Profile({"dev", "test"})`: Active when either 'dev' OR 'test' profile is active
- `@Profile("!prod")`: Active when 'prod' profile is NOT active

### Step 4: Create Profile-Specific Tests

Create tests that demonstrate different profile configurations:

**1. Development Profile Test:**

```java
@SpringBootTest
@ActiveProfiles("dev")
class DevProfileTest {
    @Autowired
    private DatabaseInfo databaseInfo;

    @Autowired
    private ApplicationInfo applicationInfo;

    @Test
    void testDevProfileConfiguration() {
        assertEquals("H2", databaseInfo.type());
        assertEquals("Development", databaseInfo.purpose());
        assertTrue(databaseInfo.consoleEnabled());
        assertEquals("dev", applicationInfo.environment());
    }
}
```

**2. Production Profile Test (Optional - Requires Docker):**

```java
@SpringBootTest
@ActiveProfiles("prod")
@Testcontainers
class ProdProfileTest {
    
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("officers_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @Autowired
    private OfficerRepository repository;

    @BeforeAll
    static void checkDockerAvailability() {
        boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        assumeTrue(dockerAvailable, "Docker is not available - skipping Testcontainers tests");
    }

    @Test
    void testPostgreSQLIntegration() {
        assertTrue(postgres.isRunning());
        
        Officer officer = new Officer(Rank.CAPTAIN, "Jean-Luc", "Picard");
        Officer saved = repository.save(officer);
        
        assertNotNull(saved.getId());
        // Clean up
        repository.delete(saved);
    }
}
```

### Step 5: Running with Different Profiles

**Command Line Activation:**

```bash
# Run with dev profile (default)
./gradlew bootRun

# Run with test profile
./gradlew bootRun --args='--spring.profiles.active=test'

# Run with production profile
./gradlew bootRun --args='--spring.profiles.active=prod'

# Run with multiple profiles
./gradlew bootRun --args='--spring.profiles.active=dev,debug'
```

**Environment Variable:**

```bash
export SPRING_PROFILES_ACTIVE=prod
./gradlew bootRun
```

**IDE Configuration:**
- IntelliJ: Run Configuration → Environment Variables → `SPRING_PROFILES_ACTIVE=dev`
- VS Code: launch.json → `"env": {"SPRING_PROFILES_ACTIVE": "dev"}`

### Step 6: Testing Different Profiles

```bash
# Test development profile
./gradlew test --tests DevProfileTest

# Test production profile (requires Docker)
./gradlew test --tests ProdProfileTest

# Run all tests
./gradlew test
```

## Key Learning Points

This exercise demonstrates several important concepts:

- **Environment Separation**: Different configurations for dev/test/prod environments
- **Conditional Bean Creation**: Using `@Profile` to create environment-specific beans
- **Property Overrides**: Profile-specific `application-{profile}.yml` files
- **External Configuration**: Using environment variables for sensitive data
- **Modern Testing**: Testcontainers for realistic database testing
- **Graceful Degradation**: Optional Docker requirements with assumption-based tests

### Profile Best Practices

- **Default Profile**: Always specify a sensible default (`spring.profiles.active=dev`)
- **Property Hierarchy**: Base properties in `application.yml`, overrides in profile-specific files
- **Environment Variables**: Use `${VAR_NAME:default}` for environment-specific values
- **Security**: Never commit production credentials to version control
- **Testing**: Use `@ActiveProfiles` to test profile-specific behavior

> [!TIP]
> Profiles are essential for professional Spring Boot applications. They enable you to maintain a single codebase while supporting multiple deployment environments with different configurations.

> [!WARNING]
> The Testcontainers examples require Docker Desktop to be installed and running. If Docker is not available, those tests will be skipped automatically using JUnit's `assumeTrue()`.

### Bonus: A Peek at Spring Boot Actuator

The `persistence` project includes the Actuator starter:

```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

With the application running, visit the production-ready endpoints it adds:

```bash
curl http://localhost:8080/actuator          # what's exposed
curl http://localhost:8080/actuator/health   # {"status":"UP"}
```

Only `health` is exposed over HTTP by default. To see more (metrics, env, loggers), add this to `application.yml` and restart:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

Then try `curl http://localhost:8080/actuator/metrics/jvm.memory.used`. Actuator is how production Spring Boot applications expose health checks and metrics to load balancers and monitoring systems — see the slides for custom health indicators and Micrometer integration.

[Back to Table of Contents](#table-of-contents)

## Optional: Aspect-Oriented Programming (AOP) with Spring

**Objective**: Learn how to use Spring AOP to implement cross-cutting concerns like logging, performance monitoring, and method timing in a clean, modular way.

> [!NOTE]
> This is an optional exercise that builds upon the `demo` project. AOP (Aspect-Oriented Programming) is a programming paradigm that allows you to modularize cross-cutting concerns that span multiple classes and methods.

### What is AOP?

Aspect-Oriented Programming allows you to separate cross-cutting concerns (like logging, security, transactions) from your business logic. Instead of scattering logging code throughout your application, you can define it once in an aspect and apply it wherever needed.

**Key AOP Concepts:**
- **Aspect**: A module that encapsulates cross-cutting concerns
- **Join Point**: A point in program execution (like method calls)
- **Pointcut**: An expression that selects join points
- **Advice**: Code that runs at selected join points (@Before, @After, @Around, etc.)

### Step 1: Add AOP Dependency

First, add the AspectJ starter to your `demo` project's `build.gradle`. (Spring Boot 4 renamed `spring-boot-starter-aop` to `spring-boot-starter-aspectj`.)

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-aspectj'  // Add this line
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

### Step 2: Create a Basic Logging Aspect

Create a new package `aspects` in your demo project and add a `LoggingAspect` class:

```java
package com.kousenit.demo.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.kousenit.demo.controllers.*.*(..))")
    public void logMethodCalls(JoinPoint joinPoint) {
        logger.info("Entering method: {}", joinPoint.getSignature());
        logger.info("with args: {}", Arrays.toString(joinPoint.getArgs()));
    }
}
```

**What this does:**
- `@Aspect`: Marks this class as an aspect
- `@Component`: Makes it a Spring-managed bean
- `@Before`: Advice that runs before method execution
- `execution(* com.kousenit.demo.controllers.*.*(..))`: Pointcut targeting all controller methods

### Step 3: Test the Basic Logging

1. Run your demo application:
   ```bash
   cd demo
   ./gradlew bootRun
   ```

2. Visit `http://localhost:8080/hello?name=AOP` in your browser

3. Check the console output - you should see logging from the aspect:
   ```
   INFO  c.k.demo.aspects.LoggingAspect - Entering method: String com.kousenit.demo.controllers.HelloController.sayHello(String,Model)
   INFO  c.k.demo.aspects.LoggingAspect - with args: [AOP, {...}]
   ```

### Step 4: Add Performance Monitoring with @Around

Add a performance monitoring method to your `LoggingAspect`:

```java
@Around("execution(* com.kousenit.demo.controllers.*.*(..))")
public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.nanoTime();
    
    try {
        // Proceed with the original method call
        Object result = joinPoint.proceed();
        
        long endTime = System.nanoTime();
        logger.info("Method {} executed in {} ms", 
                   joinPoint.getSignature().getName(), 
                   (endTime - startTime) / 1_000_000);
        
        return result;
    } catch (Exception e) {
        long endTime = System.nanoTime();
        logger.error("Method {} failed after {} ms with exception: {}", 
                    joinPoint.getSignature().getName(), 
                    (endTime - startTime) / 1_000_000, 
                    e.getMessage());
        throw e;
    }
}
```

**Important**: Comment out or remove the `@Before` advice to avoid duplicate logging, or use different pointcuts for each advice type.

### Step 5: Create a Custom Annotation for Selective Timing

Create a custom annotation for methods you want to time:

```java
package com.kousenit.demo.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Timed {
    String description() default "";
}
```

Add a corresponding aspect method:

```java
@Around("@annotation(timed)")
public Object timeAnnotatedMethods(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
    long startTime = System.nanoTime();
    
    try {
        Object result = joinPoint.proceed();
        long duration = (System.nanoTime() - startTime) / 1_000_000;
        
        String description = timed.description().isEmpty() ? 
            joinPoint.getSignature().getName() : timed.description();
        logger.info("@Timed method '{}' executed in {} ms", description, duration);
        
        return result;
    } catch (Exception e) {
        long duration = (System.nanoTime() - startTime) / 1_000_000;
        logger.error("@Timed method '{}' failed after {} ms", 
                    joinPoint.getSignature().getName(), duration);
        throw e;
    }
}
```

### Step 6: Use the @Timed Annotation

Annotate a controller method with your custom annotation:

```java
@GetMapping("/hello")
@Timed(description = "Hello page rendering")
public String sayHello(@RequestParam(required = false, defaultValue = "World") String name, 
                      Model model) {
    model.addAttribute("user", name);
    
    // Simulate some processing time
    try {
        Thread.sleep(100);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    
    return "hello";
}
```

### Step 7: Add Method-Level Logging with @AfterReturning

Add specific logging for return values:

```java
@AfterReturning(pointcut = "execution(* com.kousenit.demo.controllers.*.*(..))", 
                returning = "result")
public void logMethodReturn(JoinPoint joinPoint, Object result) {
    logger.info("Method {} returned: {}", 
               joinPoint.getSignature().getName(), result);
}
```

### Step 8: Test Your AOP Implementation

1. Run the application and test different endpoints
2. Observe the console output to see:
   - Method entry logging
   - Execution time measurements
   - Return value logging
   - Custom @Timed annotation in action

### Step 9: Write Tests for Your Aspects

Create a test to verify your aspects work correctly:

```java
package com.kousenit.demo.aspects;

import com.kousenit.demo.controllers.HelloController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.ui.Model;
import org.springframework.ui.ConcurrentModel;

import static org.mockito.Mockito.*;

@SpringBootTest
class LoggingAspectTest {

    @Autowired
    private HelloController helloController;

    @MockitoSpyBean
    private LoggingAspect loggingAspect;

    @Test
    void testAspectIsApplied() {
        // Given
        Model model = new ConcurrentModel();
        
        // When
        helloController.sayHello("TestUser", model);
        
        // Then - verify the aspect was called
        // Note: This is tricky to test directly, but we can verify the aspect bean exists
        // In a real application, you might use a test appender to capture log output
        verify(loggingAspect, atLeastOnce()).timeAnnotatedMethods(any(), any());
    }
}
```

### Understanding AOP Terminology

**Pointcut Expressions:**
- `execution(* com.example.*.*(..))` - All methods in com.example package
- `execution(public * *(..))` - All public methods
- `@annotation(Timed)` - Methods annotated with @Timed
- `within(com.example.service.*)` - All methods within service package

**Advice Types:**
- `@Before` - Runs before method execution
- `@After` - Runs after method execution (finally block)
- `@AfterReturning` - Runs after successful method execution
- `@AfterThrowing` - Runs after method throws exception
- `@Around` - Wraps method execution (most powerful)

### Key Learning Points

- **Cross-Cutting Concerns**: AOP helps separate concerns like logging, security, and monitoring from business logic
- **Declarative Programming**: Use annotations to apply aspects without modifying existing code
- **Pointcut Expressions**: Powerful pattern matching for selecting where aspects apply
- **Around Advice**: Most flexible advice type that can control method execution
- **Custom Annotations**: Create domain-specific annotations for targeted aspect application
- **Performance Monitoring**: AOP is excellent for adding timing and performance metrics
- **Clean Separation**: Business logic remains focused on business concerns

### Real-World AOP Use Cases

- **Logging and Auditing**: Track method calls, parameters, and return values
- **Performance Monitoring**: Measure execution times and identify bottlenecks
- **Security**: Check permissions before method execution
- **Transaction Management**: Spring's @Transactional uses AOP
- **Caching**: Cache method results transparently
- **Error Handling**: Centralized exception handling and logging
- **Retry Logic**: Automatically retry failed operations

> [!TIP]
> AOP is powerful but should be used judiciously. Overuse can make code harder to debug and understand. Use it for truly cross-cutting concerns that would otherwise result in code duplication.

> [!WARNING]
> Be careful with pointcut expressions - overly broad expressions can impact performance by intercepting more methods than intended.

[Back to Table of Contents](#table-of-contents)
## Optional: A Taste of Spring AI

*If time permits.* This exercise uses the `springai` project, which demonstrates [Spring AI](https://spring.io/projects/spring-ai) — the Spring approach to calling large language models. Spring AI 2.0 is built for Spring Boot 4, and its central abstraction, `ChatClient`, follows the same fluent-builder style as `RestClient` and `JdbcClient`.

### Prerequisites

- An OpenAI API key in the `OPENAI_API_KEY` environment variable. Without a key the project still compiles and its tests skip themselves, so it is safe to build the whole repository either way.

### Step 1: Examine the Build File

Open `springai/build.gradle` and note two things:

```groovy
dependencyManagement {
    imports {
        mavenBom "org.springframework.ai:spring-ai-bom:2.0.0"
    }
}

dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
    // ...
}
```

The Spring AI BOM manages the versions of all Spring AI artifacts, and the OpenAI starter auto-configures everything needed to talk to the OpenAI API — including a `ChatClient.Builder` you can inject, exactly the way `RestClient.Builder` worked in the REST client labs.

### Step 2: The ChatClient Service

Open `springai/src/main/java/com/kousenit/springai/services/SpaceService.java`:

```java
@Service
public class SpaceService {
    private final ChatClient chatClient;

    public SpaceService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    // Simple text in, text out
    public String askQuestion(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    // Structured output: the model's response is mapped onto a Java record
    public SpaceStation describeStation(String stationName) {
        return chatClient.prompt()
                .user(u -> u.text("Describe the space station named {name}")
                        .param("name", stationName))
                .call()
                .entity(SpaceStation.class);
    }

    public record SpaceStation(
            String name,
            String operator,
            int yearLaunched,
            List<String> participatingCountries) {
    }
}
```

Two ideas to notice:

1. **`askQuestion`** is the simplest possible interaction: a user message in, the model's text out.
2. **`describeStation`** shows *structured output* — Spring AI asks the model to reply as JSON matching the `SpaceStation` record and de-serializes it for you. This is the same records-as-data-carriers idiom you used for REST responses, applied to an LLM.

### Step 3: Run the Tests

```bash
cd springai
./gradlew test
```

Look at `SpaceServiceTest`: a `@BeforeEach` method calls `assumeTrue(...)` on the presence of the API key, so the tests run as live integration tests when the key is available and report as skipped when it is not — the same guard pattern the `JokeServiceTest` used for network availability.

### Key Learning Points

- Spring AI brings LLM access into the familiar Spring programming model: a starter, auto-configuration, and an injectable fluent builder
- `ChatClient` structured output maps model responses onto Java records
- `assumeTrue` keeps environment-dependent tests from failing builds where the environment is absent

[Back to Table of Contents](#table-of-contents)
