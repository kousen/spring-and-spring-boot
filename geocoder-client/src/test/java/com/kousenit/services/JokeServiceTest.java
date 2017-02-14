package com.kousenit.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JokeServiceTest {
    private Logger logger = LoggerFactory.getLogger(JokeServiceTest.class);

    @Autowired
    private JokeService service;

    @Test
    public void getJoke() throws Exception {
        String joke = service.getJoke("Craig", "Walls");
        assertThat(joke, containsString("Craig"));
        assertThat(joke, containsString("Walls"));
    }

}