package com.oreilly.restclient.services;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class JokeServiceTest {
    private final Logger logger = LoggerFactory.getLogger(JokeService.class);

//    @Autowired
//    private JokeService service;

    @Test
    public void getJoke(@Autowired JokeService service) {
        String joke = service.getJoke("Craig", "Walls");
        logger.info(joke);
        assertTrue(joke.contains("Craig") || joke.contains("Walls"));
    }
}