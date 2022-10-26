package com.oreilly.restclient.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
public class JokeServiceTest {
    private final Logger logger = LoggerFactory.getLogger(JokeService.class);

    @Autowired
    private JokeService service;

    @BeforeEach
    void setUp() throws Exception {
        HttpResponse<Void> response = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder()
                        .uri(URI.create("http://icndb.com"))
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .build(),
                        HttpResponse.BodyHandlers.discarding());
        assumeTrue(response.statusCode() == 200);
    }

    @Test
    public void getJoke() {
        String joke = service.getJoke("Craig", "Walls");
        logger.info(joke);
        assertTrue(joke.contains("Craig") || joke.contains("Walls"));
    }

    @Test
    public void getJokeRestTemplate() {
        String joke = service.getJokeRT("Greg", "Turnquist");
        logger.info(joke);
        assertTrue(joke.contains("Greg") || joke.contains("Turnquist"));
    }
}