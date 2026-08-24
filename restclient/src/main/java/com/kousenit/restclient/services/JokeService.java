package com.kousenit.restclient.services;

import com.kousenit.restclient.config.MyProperties;
import com.kousenit.restclient.json.JokeResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

@Service
public class JokeService {
    private final RestClient client;
    private final WebClient webClient;

    public JokeService(MyProperties properties) {
        String baseUrl = properties.getJokeUrl();
        client = RestClient.builder().baseUrl(baseUrl).build();
        webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public String getJoke() {
        JokeResponse response = client.get()
                .uri("/jokes/random?category=dev")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JokeResponse.class);
        return Optional.ofNullable(response)
                .map(JokeResponse::getValue)
                .orElse("No joke found");
    }

    public String getJokeReactive() {
        return webClient.get()
                .uri("/jokes/random?category=dev")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JokeResponse.class)
                .log()
                .map(JokeResponse::getValue)
                .block(Duration.ofSeconds(2));
    }
}
