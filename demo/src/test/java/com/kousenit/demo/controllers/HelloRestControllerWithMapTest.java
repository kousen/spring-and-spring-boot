package com.kousenit.demo.controllers;

import com.kousenit.demo.json.Greeting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class HelloRestControllerWithMapTest {

    @Test
    public void greetWithName(@Autowired RestTestClient client) {
        client.post().uri("/restwithmap/{name}", "Dolly")
                .exchange()
                .expectStatus().isCreated();
        client.get().uri("/restwithmap?name=Dolly")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Greeting.class)
                .isEqualTo(new Greeting("Hello, Dolly!"));
    }

    @Test
    public void greetWithoutName(@Autowired RestTestClient client) {
        client.get().uri("/restwithmap")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Greeting.class)
                .isEqualTo(new Greeting("Hello, World!"));
    }

    @Test
    public void greetWithNameDoesNotExist(@Autowired RestTestClient client) {
        client.get().uri("/restwithmap?name=abc")
                .exchange()
                .expectStatus().isNotFound();
    }
}
