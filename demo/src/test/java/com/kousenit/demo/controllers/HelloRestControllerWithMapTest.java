package com.kousenit.demo.controllers;

import com.kousenit.demo.json.Greeting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloRestControllerWithMapTest {

    @Test
    public void greetWithName(@Autowired TestRestTemplate template) {
        template.postForEntity("/restwithmap/{name}", null, Greeting.class, "Dolly");
        Greeting response = template.getForObject("/restwithmap?name=Dolly", Greeting.class);
        assertEquals("Hello, Dolly!", response.message());
    }

    @Test
    public void greetWithoutName(@Autowired TestRestTemplate template) {
        ResponseEntity<Greeting> entity = template.getForEntity("/restwithmap", Greeting.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, entity.getHeaders().getContentType());
        Greeting response = entity.getBody();
        if (response != null) {
            assertEquals("Hello, World!", response.message());
        }
    }

    @Test
    public void greetWithNameDoesNotExist(@Autowired TestRestTemplate template) {
        ResponseEntity<Greeting> entity = template.getForEntity("/restwithmap?name=abc", Greeting.class);
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }
}
