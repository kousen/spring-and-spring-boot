package com.kousenit.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JokeServiceTest {

    @Autowired
    private JokeService service;

    @Test
    public void getJoke() {
        String joke = service.getJoke("Craig", "Walls");
        assertThat(joke).contains("Craig");
        assertThat(joke).contains("Walls");
    }

}