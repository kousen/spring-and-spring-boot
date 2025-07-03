# Spring Boot Labs

## Creating a New Project

1. Go to http://start.spring.io to access the Spring Initializr
2. In the "Generate a" drop-down, switch from "Maven Project" to "Gradle Project"
3. Specify the Group as `com.oreilly` and the Artifact as `demo`
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
9. Open the file `src/main/java/com/oreilly/demo/DemoApplication.java` and note that it contains a standard Java "main" method (with signature: `public static void main(String[] args)`)
10. Start the application by running this method. There won't be any web components available yet, but you can see the start up of the application in the command window.
11. Add a controller by creating a file called `com.oreilly.demo.controllers.HelloController` in the `src/main/java` directory

> **Note:** The goal is to have the `HelloController` class in the `com.oreilly.demo.controllers` package starting at the root directory `src/main/java`

12. The code for the `HelloController` is:

```java
package com.oreilly.demo.controllers;

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
26. Because the controller is a simple POJO, you can unit test it by simply instantiating the controller and calling its `sayHello` method directly. To do so, add a class called `HelloControllerUnitTest` to the `com.oreilly.demo.controllers` package in the _test_ folder, `src/test/java`
27. The code for the test class is:

```java
package com.oreilly.demo.controllers;

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
29. To perform an integration test instead, use the `MockMVC` classes available in Spring. Create a new class called `HelloControllerMockMVCTest` in the `com.oreilly.demo.controllers` package in `src/test/java`
30. The code for the integration test is:

```java
package com.oreilly.demo.controllers;

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

1. Add another class to the `com.oreilly.demo.controllers` package called `HelloRestController`. This controller will be used to model a RESTful web service, though at this stage it will be limited to HTTP GET requests (for reasons explained below).
2. Add the `@RestController` annotation to the class.
3. By default, REST controllers will serialize and deserialize Java classes into JSON data using the Jackson 2 JSON library, which is currently on the classpath by default. To have an object (other than a trivial `String`) to serialize, add a class called `Greeting` to the `com.oreilly.demo.json` package. In a larger application, this would represent a domain class that you can store in a database or other persistent storage mechanism.
4. In the `Greeting` class, add a private attribute of type `String` called `message`.
5. Add a `getMessage` method for the `greeting` attribute that returns the current message.
6. Add a constructor to `Greeting` that takes a `String` argument and saves it to the attribute.
7. Add a default constructor that does nothing. This constructor will be used by the JSON parser to convert a JSON response into an instance of `Greeting`.
8. Add an `equals` method, a `hashCode` method, and a `toString` method in the usual manner. A reasonable version would be:

```java
package com.oreilly.demo.json;

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
package com.oreilly.hello.controllers;

import com.oreilly.hello.json.Greeting;
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

This exercise uses the new reactive web client called, naturally enough, `WebClient`, to access a RESTful web service. The template is used to convert the response into an object for the rest of the system. Older Spring applications used `RestTemplate` for synchronous access, but that class is in the process of being gradually replaced with `WebClient`. Since `WebClient` is used for reactive applications, it returns responses of type `Mono` and `Flux`, which will be discussed briefly in class. They are essentially "promises" that return a single object (for `Mono`) or a collection (for `Flux`) of objects.

1. Create a new Spring Boot project (either by using the Initializr at http://start.spring.io or using your IDE) called `restclient`. Add the _Spring Reactive Web_ dependency, but no others are necessary.
2. Create a service class called `AstroService` in a `com.oreilly.restclient.services` package under `src/main/java`
3. Add the annotation `@Service` to the class (from the `org.springframework.stereotype` package, so you'll need an `import` statement)
4. Add a private attribute to `AstroService` of type `WebClient` (from `org.springframework.web.reactive.function.client` package) called `client`
5. Add a constructor to `AstroService` that takes a single argument of type `WebClient.Builder` called `builder`.
6. Inside the constructor, set the base URL using the `baseUrl("http://api.open-notify.org")` method on the builder, then invoke `build()` method and assign the result to the `client` attribute. The argument will be _autowired_ into the constructor from the application context.

> **Note:** If you provide only a single constructor in a class, Spring will inject all the arguments automatically. There is no harm, however, in adding the annotation `@Autowired` to the constructor if you wish.

7. The site providing the Astro API is http://api.open-notify.org, which processes NASA data. One of its services returns the list of astronauts currently in space.
8. Add a `public` method to the service called `getAstroResponse`.
9. The `WebClient` class provides a fluent interface for making a call to a restful web service. This will require creating Java classes that map to the JSON structure. A typical example of the JSON response is:

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

10. Each of the two JSON objects needs to be mapped to a class. Create a class called `Assignment` in the `com.oreilly.restclient.json` package that maps to the JSON object assigned to `value`, as shown:

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

11. Add another class called `AstroResponse` as shown below. You could use annotations from the included Jackson 2 JSON parser to map the properties to different attribute names, but in this case it's easy enough to make them the same.

```java
package com.oreilly.restclient.json;

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

12. Now the JSON response from the web service can be converted into an instance of the `AstroResponse` class. The following code should be added to the `getAstroResponse` method to do so:

```java
return client.get()
                .uri(uriBuilder -> uriBuilder.path("/astros.json").build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AstroResponse.class)
                .block(Duration.ofSeconds(2));
```

13. This method retrieves the JSON response and converts it to an instance of the `AstroResponse` class via the `bodyToMono` method, after blocking a maximum of two seconds until the response is received.

> **Note:** In a reactive application, the return type would have been `Mono<AstroResponse>` and the client would be responsible for waiting for a response to be received. In this case, the client is not aware that a reactive web client is being used to retrieve the response.

14. To demonstrate how to use the service, create a JUnit 5 test for it. Create a class called `AstroServiceTest` in the `com.oreilly.services` package under the test hierarchy, `src/test/java`.
15. The source for the test is:

```java
package com.oreilly.restclient.services;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AstroServiceTest {
    private final Logger logger = LoggerFactory.getLogger(AstroService.class);

    @Autowired
    private AstroService service;

    @Test
    public void getAstroResponse() {
        AstroResponse response = service.getAstroResponse();
        logger.info(response.toString());
        assertTrue(response.getNumber() >= 0);
        assertEquals("success", response.getMessage());
        assertEquals(response.getNumber(), response.getPeople().size());
    }

}
```

16. Note the use of the SLF4J `Logger` class to log the responses to the console. Not everything in Spring needs to be injected. Spring includes multiple loggers in the classpath. This example uses SLF4J.
17. Execute the test and make any needed corrections until it passes.

## Http Interfaces (Spring Boot 3+ only)

If you are using Spring Boot 3.0 or above (and therefore Spring 6.0 or above), there is a new way to access external restful web services. The [Spring 6 documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#spring-integration) has a section on REST clients, which includes the `RestTemplate` and `WebClient` classes discussed above, as well as something called HTTP Interface.

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

## Accessing the Google Geocoder

Google provides a free geocoding web service that converts addresses into geographical coordinates.

This exercise uses the `WebClient` to access the Google geocoder and converts the responses into Java objects.

1. The documentation for the Google geocoder is at https://developers.google.com/maps/documentation/geocoding/intro. Take a look at the page there to see how the geocoder is intended to be used. The base URL for the service is (assuming you want JSON responses) https://maps.googleapis.com/maps/api/geocode/json?address=street,city,state. The `address` parameter needs to be URL encoded and the parts of the address are joined using commas.

> **Note:** The address components can be anything appropriate to the host country. The URL includes a string which separates the values by commas. The components don't have to be street, city, and state.

2. Rather than creating a new project, we'll add a `GeocoderService` to the existing `restclient` project. In that project, add the new class to the `services` package
3. Add the `@Service` annotation to the class so that Spring will automatically load and manage the bean during its component scan at start up.
4. Give the class an attribute of type `WebClient` called `client`
5. Add a constructor to the class that takes an argument of type `WebClient.Builder` called `builder`
6. Inside the constructor, set the value of the `client` field by setting the base URL using `baseUrl("https://maps.googleapis.com")` and invoking the `build` method on the builder.
7. Map the JSON response to classes in a `json` package. The JSON response for the URL https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,Mountain+View,CA is:

```javascript
{
   "results" : [
      {
         "address_components" : [
            {
               "long_name" : "1600",
               "short_name" : "1600",
               "types" : [ "street_number" ]
            },
            {
               "long_name" : "Amphitheatre Pkwy",
               "short_name" : "Amphitheatre Pkwy",
               "types" : [ "route" ]
            },
            {
               "long_name" : "Mountain View",
               "short_name" : "Mountain View",
               "types" : [ "locality", "political" ]
            },
            {
               "long_name" : "Santa Clara County",
               "short_name" : "Santa Clara County",
               "types" : [ "administrative_area_level_2", "political" ]
            },
            {
               "long_name" : "California",
               "short_name" : "CA",
               "types" : [ "administrative_area_level_1", "political" ]
            },
            {
               "long_name" : "United States",
               "short_name" : "US",
               "types" : [ "country", "political" ]
            },
            {
               "long_name" : "94043",
               "short_name" : "94043",
               "types" : [ "postal_code" ]
            }
         ],
         "formatted_address" : "1600 Amphitheatre Parkway, Mountain View, CA 94043, USA",
         "geometry" : {
            "location" : {
               "lat" : 37.4224764,
               "lng" : -122.0842499
            },
            "location_type" : "ROOFTOP",
            "viewport" : {
               "northeast" : {
                  "lat" : 37.4238253802915,
                  "lng" : -122.0829009197085
               },
               "southwest" : {
                  "lat" : 37.4211274197085,
                  "lng" : -122.0855988802915
               }
            }
         },
         "place_id" : "ChIJ2eUgeAK6j4ARbn5u_wAGqWA",
         "types" : [ "street_address" ]
      }
   ],
   "status" : "OK"
}
```

We don't care about the address components, though the formatted address looks useful. In a `json` subpackage, create the following classes:

```java
package com.oreilly.restclient.json;

import java.util.List;

public class Response {
    private List<Result> results;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public Location getLocation() {
        return results.get(0).getGeometry().getLocation();
    }

    public String getFormattedAddress() {
        return results.get(0).getFormattedAddress();
    }
}

package com.oreilly.restclient.json;

public class Result {
    private String formattedAddress;
    private Geometry geometry;

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}

package com.oreilly.restclient.json;

public class Geometry {
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}

package com.oreilly.restclient.json;

public class Location {
    private double lat;
    private double lng;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String toString() {
        return String.format("(%s,%s)", lat, lng);
    }
}
```

8. In the `GeocoderService` class, add constants for the key.

```java
private static final String KEY = "AIzaSyDw_d6dfxDEI7MAvqfGXEIsEMwjC1PWRno";
```

9. Add a `public` method that formulates the complete URL with an encoded address and converts it to a `Response` object. The code is simple if you are using Java 11:

```java
public Site getLatLng(String... address) {
    String encoded = Stream.of(address)
        .map(component -> URLEncoder.encode(component, StandardCharsets.UTF_8))
        .collect(Collectors.joining(","));
    String path = "/maps/api/geocode/json";
    Response response = client.get()
        .uri(uriBuilder ->
                uriBuilder.path(path)
                    .queryParam("address", encoded)
                    .queryParam("key", KEY)
                    .build())
        .retrieve()
        .bodyToMono(Response.class)
        .block(Duration.ofSeconds(2));
    return new Site(response.getFormattedAddress(),
        response.getLocation().getLat(),
        response.getLocation().getLng());
}
```

10. If, however, you are still on Java 8, then the `StandardCharsets` class is not available, and the `encode` version you have to use instead throws a checked exception. In that case, use the following instead:

```java
private String encodeString(String s) {
    try {
        return URLEncoder.encode(s,"UTF-8");
    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    }
    return s;
}

public Site getLatLng(String... address) {
    String encoded = Stream.of(address)
        .map(this::encodeString)
        .collect(Collectors.joining(","));
    String path = "/maps/api/geocode/json";
    Response response = client.get()
        .uri(uriBuilder ->
                uriBuilder.path(path)
                    .queryParam("address", encoded)
                    .queryParam("key", KEY)
                    .build())
        .retrieve()
        .bodyToMono(Response.class)
        .block(Duration.ofSeconds(2));
    return new Site(response.getFormattedAddress(),
        response.getLocation().getLat(),
        response.getLocation().getLng());
}
```

The use of the `private` method is to avoid the try/catch block inside the `map` method directly, just to improve readability.

11. To use this service, we need an entity called `Site`. Add a POJO to the `com.oreilly.restclient.entities` package called `Site` that wraps a formatted address string and doubles for the latitude and longitude. The code is:

```java
package com.oreilly.restclient.entities;

public class Site {

    private Integer id;
    private String address;
    private double latitude;
    private double longitude;

    public Site() {}

    public Site(String address, double latitude, double longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setName(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Site{" +
                "address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
```

12. Now we need a test to make sure this is working properly. Add a test class called `GeocoderServiceTest` to the `com.oreilly.restclient.services` package in the test directory `src/test/java`.
13. Add the test annotation to the test:

```java
@SpringBootTest
```

14. Autowire in the `GeocoderService` into a field called `service`
15. Add two tests: one using a city and state of Boston, MA, and one using a street address of 1600 Ampitheatre Parkway, Mountain View, CA. The tests are:

```java
@Test
public void getLatLngWithoutStreet() {
    Site site = service.getLatLng("Boston", "MA");
    assertAll(
        () -> assertEquals(42.36, site.getLatitude(), 0.01),
        () -> assertEquals(-71.06, site.getLongitude(), 0.01)
    );
}

@Test
public void getLatLngWithStreet() throws Exception {
    Site site = service.getLatLng("1600 Ampitheatre Parkway",
            "Mountain View", "CA");
    assertAll(
        () -> assertEquals(37.42, site.getLatitude(), 0.01),
        () -> assertEquals(-122.08, site.getLongitude(), 0.01)
    );
}
```

16. Run the tests and make sure they pass.
17. We actually still have a problem. To see it, log the returned `Site` object to the console. First add a SLF4J logger to the `GeocoderServiceTest`

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ...

private Logger logger = LoggerFactory.getLogger(GeocoderServiceTest.class);
```

18. Then, in the test methods, log the site.

```java
@Test
public void getLatLngWithoutStreet() {
    Site site = service.getLatLng("Boston", "MA");
    logger.info(site.toString());
    // ... asserts as before ...
}
```

19. Run either or both of the tests and look at the logged site(s).
20. The address fields of the sites are null! That's because our `Result` class has a `String` field called `formattedAddress`, but the JSON response uses underscores instead of camel case (i.e., `formatted_address`).

There are a couple of different ways to solve this. As a one-time fix, you can add an annotation to the `formatted_address` field in the `Result` class

```java
import com.fasterxml.jackson.annotation.JsonProperty;

public class Result {
    @JsonProperty("formatted_address")
    private String formattedAddress;

    // ... rest as before ...
```

The `@JsonProperty` annotation is a general purpose mechanism you can use whenever the property in the bean does not match the JSON field. Run your test again and see that the `name` value in the `Site` is now correct.

21. The other way to fix the issue is to set a global property that converts all camel case properties to underscores during the JSON parsing process. To use this, first remove the `@JsonProperty` annotation from `Result`.
22. We will then add the required property to a YAML properties file. By default, Spring Boot generates a file called `application.properties` in the `src/main/resources` folder. Rename that file to `application.yml`
23. Inside `application.yml`, add the following setting:

```yml
spring:
  jackson:
    property-naming-strategy: SNAKE_CASE
```

24. Once again run the tests and see that the `address` field in `Site` is set correctly. The advantage of the YAML file is that you can nest multiple properties without too much code duplication.

In principle, now we could save the `Site` instances in a database (generating id values in the process), and since they have latitudes and longitudes, we could then plot them on a map.

## Using the JDBC template

Spring provides a class called `JdbcTemplate` in the `org.springframework.jdbc.core` package. All it needs in order to work is a data source. It removes almost all the boilerplate code normally associated with JDBC. In this exercise, you'll use the `JdbcTemplate` to implement the standard CRUD (create, read, update, delete) methods on an entity.

1. Make a new Spring Boot project with group `com.oreilly` and artifact called `persistence` using the Spring Initializr. Generate a Gradle build file and select the JPA dependency, which will include JDBC. Also select the H2 dependency, which will provide a JDBC driver for the H2 database as well as a connection pool.
2. Import the project into your IDE in the usual manner.
3. For this exercise, as well as the related exercises using JPA and Spring Data, we'll use a domain class called `Officer`. An `Officer` will have a generated `id` of type `Integer`, strings for `firstName` and `lastName`, and a `Rank`. The `Rank` will be a Java enum.
4. First define the `Rank` enum in the `com.oreilly.persistence.entities` package and give it a few constants:

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
        if (first != null ? !firstName.equals(officer.firstName) : officer.firstName != null) return false;
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

6. One of the features of Spring Boot is that you can create and populate database tables by define scripts with the names `schema.sql` and `data.sql` in the `src/main/resources` folder. First define the database table in `schema.sql`

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

7. Next populate the table by adding the following `INSERT` statements in `data.sql`

```sql
INSERT INTO officers(rank, first_name, last_name) VALUES('CAPTAIN', 'James', 'Kirk');
INSERT INTO officers(rank, first_name, last_name) VALUES('CAPTAIN', 'Jean-Luc', 'Picard');
INSERT INTO officers(rank, first_name, last_name) VALUES('CAPTAIN', 'Benjamin', 'Sisko');
INSERT INTO officers(rank, first_name, last_name) VALUES('CAPTAIN', 'Kathryn', 'Janeway');
INSERT INTO officers(rank, first_name, last_name) VALUES('CAPTAIN', 'Jonathan', 'Archer');
```

8. When Spring starts up, the framework will automatically create a DB connection pool based on the H2 driver and then create and populate the database tables for you. Now we need a DAO (data access object) interface holding the CRUD methods that will be implemented in the different technologies. Define a Java interface called `OfficerDAO` in the `com.oreilly.persistence.dao` package.

```java
package com.oreilly.persistence.dao;

import com.oreilly.persistence.entities.Officer;

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

9. In this exercise, implement the interface using the `JdbcTemplate` class. Start by creating a class in the `com.oreilly.persistence.dao` package called `JdbcOfficerDAO`.
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

15. Now for the finder methods. When a SQL query produces a `ResultSet`, the template asks for an implementation of the `RowMapper` interface as another argument to the `queryForObject` method. This interface has a single abstract method called `mapRow`, which takes the `ResultSet` and a row number as arguments. The implementation then uses the arguments to convert a row of the result set into a instance of the domain class. To do this, here implement `findById` method in terms of a query using a standard anonymous inner class that works in Java 7 and below for the `RowMapper`

```java
@Override
public Optional<Officer> findById(Integer id) {
    try (Stream<Officer> stream =
            jdbcTemplate.queryForStream(
                "select * from officers where id=?",
                new RowMapper<Officer>() {  // Java 7 anonymous inner class
                    @Override
                    public Officer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Officer(rs.getInt("id"),
                                Rank.valueOf(rs.getString("rank")),
                                rs.getString("first_name"),
                                rs.getString("last_name"));
                    }
                },
                id)) {
        return stream.findFirst();
    }
}
```

16. The same row mapper can be used to find all the instances of `Officer`. The `JdbcTemplate` uses the `query` method to automatically iterate over the result set, calling the row mapper for each row to convert it to an `Officer`, and ultimately returns a collection of officers. This time, however, take advantage of Java 8 by using a lambda expression to implement the row mapper.

```java
@Override
public List<Officer> findAll() {
    return jdbcTemplate.query("SELECT * FROM officers",
            (rs, rowNum) -> new Officer(rs.getInt("id"), // Java 8 lambda expression
                    Rank.valueOf(rs.getString("rank")),
                    rs.getString("first_name"),
                    rs.getString("last_name")));
}
```

The row mapper implementation is exactly the same, but uses a Java 8 lambda expression rather than the anonymous inner class. The return type is a `Collection<Officer>`

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

24. The rest of the tests are pretty straightforward, other than the fact we will use Java 8 constructs to implement them.

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

## Implementing the CRUD layer using JPA

The Java Persistence API (JPA) is a layer over the so-called persistence providers, the most common of which is Hibernate. With regular Spring, configuring JPA requires several beans, including an entity manger factory and a JPA vendor adapter. Fortunately, in Spring Boot, the presence of the JPA dependency causes the framework to implement all of that for you.

1. To use JPA, we need an entity. We'll use the same `Officer` class from the previous exercise, but this time we will add the appropriate JPA annotations `@Entity`, `@Id`, `@GeneratedValue`, `@Table`, and `@Column`

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

// ... rest as before ...
}
```

The `@Enumerated` annotation tells Hibernate to store the value of the enum as a string rather than an index.

2. Create a class called `JpaOfficerDAO` that implements the `OfficerDAO` interface and adds an `EntityManagerFactory` as an attribute

```java
@Repository
public class JpaOfficerDAO implements OfficerDAO {
    @PersistenceContext
    private EntityManager entityManager;

// ... more to come ...
}
```

The `@PersistenceContext` annotation is used to inject an entity manager into the DAO. Normally we would also need to make the class transactional, but in keeping with common practice that can be handled in a service layer. In this particular case, however, we'll so the transactions in the tests

3. The implementations of the individual methods is very simple. Since this is a course on Spring and not on JPA, they are given here without comment. Add them to the `JpaOfficerDAO` class

```java
@Override
public Officer save(Officer officer) {
    entityManager.persist(officer);
    return officer;
}

@Override
public Optional<Officer> findById(Integer id) {
    return Optional.ofNullable(entityManager.find(Officer.class, id));
}

@Override
public List<Officer> findAll() {
    return entityManager.createQuery("select o from Officer o", Officer.class)
                        .getResultList();
}

@Override
public long count() {
    return entityManager.createQuery("select count(o.id) from Officer o", Long.class)
                        .getSingleResult();
}

@Override
public void delete(Officer officer) {
    entityManager.remove(officer);
}

@Override
public boolean existsById(Integer id) {
    Object result = entityManager.createQuery(
            "SELECT 1 from Officer o where o.id=:id")
                                 .setParameter("id", id)
                                 .getSingleResult();
    return result != null;
}
```

4. The same tests used to check the `JdbcOfficerDAO` can be done again, just using a different DAO as the class under test, with one exception:

```java
@SpringBootTest
@Transactional
public class JpaOfficerDAOTest {
    @Autowired
    private JpaOfficerDAO dao;

    @Autowired
    private JdbcTemplate template;

    // private method to retrieve the current ids in the database
    private List<Integer> getIds() {
        return template.query("select id from officers", (rs, num) -> rs.getInt("id"));
    }

    @Test
    public void testSave() throws Exception {
        Officer officer = new Officer(Rank.LIEUTENANT, "Nyota", "Uhuru");
        officer = dao.save(officer);
        assertNotNull(officer.getId());
    }

    @Test
    public void findOneThatExists() throws Exception {
        getIds().forEach(id -> {
            Optional<Officer> officer = dao.findById(id);
            assertTrue(officer.isPresent());
            assertEquals(id, officer.get().getId());
        });
    }

    @Test
    public void findOneThatDoesNotExist() throws Exception {
        Optional<Officer> officer = dao.findById(999);
        assertFalse(officer.isPresent());
    }

    @Test
    public void findAll() throws Exception {
        List<String> dbNames = dao.findAll().stream()
                                  .map(Officer::getLastName)
                                  .collect(Collectors.toList());
        assertThat(dbNames).contains("Kirk", "Picard", "Sisko", "Janeway", "Archer");
    }

    @Test
    public void count() throws Exception {
        assertEquals(5, dao.count());
    }

    @Test
    public void delete() throws Exception {
        getIds().forEach(id -> {
            Optional<Officer> officer = dao.findById(id);
            assertTrue(officer.isPresent());
            dao.delete(officer.get());
        });
        assertEquals(0, dao.count());
    }

    @Test
    public void existsById() throws Exception {
        getIds().forEach(id -> assertTrue(dao.existsById(id)));
    }
}
```

Because there are now two separate beans available to Spring that implement the same `OfficerDAO` interface, the `@Autowired` annotation would fail, claiming it expected a single bean of that type but found two. The `@Qualifier` annotation is used to tell Spring the name of the bean to inject. _Several of the tests are going to fail_, however, because we have one other setting we have to modify

5. If you run the tests, you see that we quickly run into a problem, which is that the sample data is not there! This is because, by default, Hibernate is in what is called "create-drop" mode, which means it drops the database after each execution and re-creates it on startup. We can prevent that, however, by adding a setting to the `application.yml` file:

```yaml
spring:
    jpa:
        hibernate:
            ddl-auto: update
        show-sql: true
        properties:
            hibernate.format_sql: true
```

We switched the `spring.jpa.hibernate.ddl-auto` property to `update` (other options are `create`, `create-drop`, and `validate`), which will add columns as necessary but not drop any tables or data. We are also logging the generated SQL and formatting it as well.

6. There's one step of clean up required, however. This test should pass, but the `JdbcOfficerDAOTest` won't because we have to add the `@Qualifier` there, too.

```java
public class JdbcOfficerDAOTest {
    @Autowired @Qualifier("jdbcOfficerDAO")
    private OfficerDAO dao;
```

Now both tests should work properly.

## Using Spring Data

The Spring Data JPA project makes it incredibly easy to implement a DAO layer. You extend the proper interface, and the underlying infrastructure generates all the implementations for you.

Spring Data is a large, powerful API. In this exercise, we'll just show the basics.

1. Since we created this project based on the Spring Data JPA dependency, we don't need to modify the Gradle build file to add it. Note that the build file already includes the required dependencies:

```groovy
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	runtimeOnly 'com.h2database:h2'
	testImplementation('org.springframework.boot:spring-boot-starter-test')
}
```

2. Spring Data works by defining an interface that extends one of a few provided interfaces, where you specify the domain class and its primary key type. Therefore, create an interface called `OfficerRepository` in the `com.oreilly.persistence.dao` package

```java
public interface OfficerRepository extends JpaRepository<Officer, Integer> {
}
```

The interface can extend `CrudRepository`, `PagingAndSortingRepository`, or, as here, `JpaRepository`. You only have to specify the two generic parameters that represent the domain class and the primary key type. Here we use `Officer` and `Integer`.

The framework will now generate the implementations of about a dozen different methods, including all the methods listed in the `OfficerDAO` interface (which is why those methods were chosen in the first place)

3. The test class is similar to the others, except that it's written in terms of the `OfficerRepository` bean. Simply copy the existing `JpaOfficerDAOTest` class in `src/test/java` into a class called `OfficerRepositoryTest` in the same package and change the autowired repository to be of type `OfficerRepository`.

```java
@SpringBootTest
@Transactional
public class OfficerRepositoryTest {
    @Autowired
    private OfficerRepository repository;

// ... more to come ...
}
```

4. All the tests should pass, as before.
5. If you have time, you can use the Spring Data feature where it will will generate queries based on a naming convention. Simply add methods to the `OfficerRepository` interface of the form `findAllBy<property>` and you can use `And` or `Or` to chain where clauses together. For example, to find officers by their last names and by their rank, just add the following methods:

```java
List<Officer> findByRank(Rank rank);
List<Officer> findAllByLastNameLikeAndRank(String like, Rank rank);
```

6. If you want to see the H2 console, add the Spring DevTools dependency and the Web dependency to your project. Then, to be sure to see the proper URL for the database (assuming you don't set it in `application.properties`), add the following log level:

```java
logging.level.org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration=debug
```

7. Then you can go to "http://localhost:8080/db-console" and log in with the URL shown in the log, the user name "sa", and no password.
8. Once the tests are running, add two dependencies to the Gradle build file: one for the Spring Data Rest project (which will expose the data via a REST interface) and for the HAL browser, which will give us a convenient client to use

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-rest'
implementation 'org.springframework.data:spring-data-rest-hal-explorer'
```

9. After rebuilding the project, start up the application (using the class with the main method) and navigate to http://localhost:8080. Spring will insert the HAL browser at that point to allow you to add, update, and remove individual elements, which we'll do in class.