package com.kousenit.services;

import com.kousenit.entities.Joke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class JokeService {
    private Logger logger = LoggerFactory.getLogger(JokeService.class);
    private static final String BASE =
            "http://api.icndb.com/jokes/random?limitTo=[nerdy]";

    private RestTemplate restTemplate;

    @Autowired
    public JokeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getJoke(String first, String last) {
        String url = String.format("%s&firstName=%s&lastName=%s", BASE, first, last);
        Joke joke = restTemplate.getForObject(url, Joke.class);
        logger.info(String.format("%s: %s", url, joke));
        return joke.getValue().getJoke();
    }
}
