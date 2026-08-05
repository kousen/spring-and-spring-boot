package com.kousenit.springai.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class SpaceServiceTest {
    private final Logger logger = LoggerFactory.getLogger(SpaceServiceTest.class);

    @Autowired
    private SpaceService service;

    @BeforeEach
    void checkForApiKey() {
        String key = System.getenv("OPENAI_API_KEY");
        assumeTrue(key != null && !key.isBlank(),
                "OPENAI_API_KEY not set; skipping Spring AI tests");
    }

    @Test
    void askQuestion() {
        String answer = service.askQuestion(
                "In one sentence, what is the International Space Station?");
        logger.info(answer);
        assertNotNull(answer);
        assertTrue(answer.toLowerCase().contains("space"));
    }

    @Test
    void describeStation() {
        SpaceService.SpaceStation station = service.describeStation("ISS");
        logger.info(station.toString());
        assertNotNull(station.name());
        assertTrue(station.yearLaunched() >= 1998);
        assertNotNull(station.participatingCountries());
    }
}
