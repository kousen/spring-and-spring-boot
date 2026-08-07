package com.kousenit.restclient.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class OpenRouterServiceTest {
    private final Logger logger = LoggerFactory.getLogger(OpenRouterServiceTest.class);

    @Autowired
    private OpenRouterService service;

    @BeforeEach
    void checkForApiKey() {
        String key = System.getenv("OPENROUTER_API_KEY");
        assumeTrue(key != null && !key.isBlank(),
                "OPENROUTER_API_KEY not set; skipping OpenRouter test");
    }

    @Test
    void chatWithDefaultFreeRouter() {
        // openrouter/free picks a free model; the log shows which one answered
        String answer = service.chat(
                "Why do Java developers wear glasses? Answer in one sentence.");
        logger.info("Answer: {}", answer);
        assertThat(answer).isNotBlank();
    }

    @Test
    void chatWithExplicitModel() {
        // Same code, different model string — here OpenAI's open-weights model
        String answer = service.chat("openai/gpt-oss-20b:free",
                "In one sentence, what is an open-weights language model?");
        logger.info("Answer: {}", answer);
        assertThat(answer).isNotBlank();
    }
}
