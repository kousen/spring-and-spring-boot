package com.kousenit.demo.controllers;

import com.kousenit.demo.json.Greeting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class HelloRestControllerIntegrationTest {

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

    @Test
    void postGreeting(@Autowired RestTestClient client) {
        Greeting input = new Greeting("Hello, World!");
        client.post().uri("/rest")
                .contentType(MediaType.APPLICATION_JSON)
                .body(input)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Greeting.class)
                .isEqualTo(new Greeting("HELLO, WORLD!"));
    }
}
