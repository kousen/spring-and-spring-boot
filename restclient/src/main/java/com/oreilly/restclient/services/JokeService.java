package com.oreilly.restclient.services;

import com.oreilly.restclient.json.JokeResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@SuppressWarnings("HttpUrlsUsage")
@Service
public class JokeService {
    private final WebClient client;

    public JokeService(WebClient.Builder builder) {
        client = builder.baseUrl("http://api.icndb.com").build();
    }

    public String getJoke(String first, String last) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/jokes/random")
                        .queryParam("limitTo", "[nerdy]")
                        .queryParam("firstName", first)
                        .queryParam("lastName", last)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JokeResponse.class)
                .map(jokeResponse -> jokeResponse.getValue().getJoke())
                .block(Duration.ofSeconds(2));
    }
}
