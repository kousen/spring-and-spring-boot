package com.oreilly.services;

import com.oreilly.json.Joke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
@Service
public class JokeService {
    private static final String BASE = "http://api.icndb.com/jokes/random?limitTo=[nerdy]";
    private RestTemplate restTemplate;
    private Logger logger = LoggerFactory.getLogger(JokeService.class);

    public JokeService(RestTemplateBuilder builder) {
        restTemplate = builder.build();
    }

    public String getJoke(String first, String last) {
        String url = String.format("%s&firstName=%s&lastName=%s", BASE, first, last);
        Joke joke = restTemplate.getForObject(url, Joke.class);
        logger.info(joke.getValue().getJoke());
        return joke.getValue().getJoke();
    }
}
