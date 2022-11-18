package com.kousenit.services;

import com.kousenit.entities.Joke;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RestClientTest {
    private final Logger logger = LoggerFactory.getLogger(RestClientTest.class);

    @Autowired
    private RestTemplate template;

    @Test
    public void testICNDB() {
        Joke joke = template.getForObject(
                "http://api.icndb.com/jokes/random?limitTo=[nerdy]",
                Joke.class);
        logger.info(joke.getValue().getJoke());
        assertThat(joke.getType()).contains("success");
    }

    @TestConfiguration
    static class Config {
        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder()
                    .additionalMessageConverters(new GsonHttpMessageConverter());
        }
    }
}
