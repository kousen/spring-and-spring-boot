package com.oreilly.restclient.services;

import com.oreilly.restclient.json.AstroResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    public void getJokeRestTemplate() {
        AstroResponse response = service.getAstroResponseRT();
        logger.info(response.toString());
        assertTrue(response.getNumber() >= 0);
        assertEquals("success", response.getMessage());
        assertEquals(response.getNumber(), response.getPeople().size());
    }
}