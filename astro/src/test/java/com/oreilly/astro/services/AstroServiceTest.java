package com.oreilly.astro.services;

import com.oreilly.astro.entities.AstroResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AstroServiceTest {
    private Logger logger = LoggerFactory.getLogger(AstroServiceTest.class);

    @Autowired
    private AstroService service;

    @Test
    public void getAstronauts() {
        AstroResponse response = service.getAstronauts();
        logger.info(response.toString());
        assertEquals("success", response.getMessage());
        assertTrue(response.getNumber() >= 0);
        assertEquals(response.getNumber(), response.getPeople().size());
    }
}