package com.oreilly.restclient.services;

import com.oreilly.restclient.config.MyProperties;
import com.oreilly.restclient.json.JokeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

@Service
public class JokeService {
    private final WebClient client;
    private final RestTemplate template;
    private final String baseUrl;

    @Autowired
    public JokeService(WebClient.Builder builder, RestTemplateBuilder restTemplateBuilder,
                       MyProperties properties) {
        baseUrl = properties.getJokeUrl();
        client = builder.baseUrl(baseUrl).build();
        template = restTemplateBuilder.build();
    }

    public String getJokeRT() {
        String url = baseUrl + "/jokes/random?category=dev";
        JokeResponse response = template.getForObject(url, JokeResponse.class);
        return Optional.ofNullable(response)
                .map(JokeResponse::getValue)
                .orElse("No joke found");
    }

    public String getJoke() {
        return client.get()
                .uri("/jokes/random?category=dev")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JokeResponse.class)
                .log()
                .map(JokeResponse::getValue)
                .block(Duration.ofSeconds(2));
    }
}
