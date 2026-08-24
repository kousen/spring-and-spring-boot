package com.kousenit.restclient.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opentest4j.TestAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

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
        // Same code, different model string — here NVIDIA's open-weights Nemotron.
        // Free-tier slugs churn; if this 404s, pick a current one from
        // https://openrouter.ai/models?max_price=0
        String answer;
        try {
            answer = service.chat("nvidia/nemotron-3-super-120b-a12b:free",
                    "In one sentence, what is an open-weights language model?");
        } catch (HttpClientErrorException.TooManyRequests e) {
            // The shared free-tier pool throttles unpredictably; not a code problem
            throw new TestAbortedException("Free tier rate-limited upstream; skipping", e);
        }
        logger.info("Answer: {}", answer);
        assertThat(answer).isNotBlank();
    }
}
