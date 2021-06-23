package com.oreilly.restclient.services;

import com.oreilly.restclient.json.JokeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;
import java.time.Duration;

@SuppressWarnings("HttpUrlsUsage")
@Service
public class JokeService {
    private final WebClient client;
    private final RestTemplate template;

    @Autowired
    public JokeService(WebClient.Builder builder, RestTemplateBuilder restTemplateBuilder) {
        client = builder.baseUrl("http://api.icndb.com").build();
        template = restTemplateBuilder.build();
    }

    public String getJokeRT(String first, String last) {
        String url = "http://api.icndb.com/jokes/random?limitTo=[nerdy]&firstName="
                + first + "&lastName=" + last;
        return template.getForObject(url, JokeResponse.class).getValue().getJoke();
    }

//    public String getJokeHttpClient(String first, String last) {
//        String url = "http://api.icndb.com/jokes/random?limitTo=[nerdy]&firstName="
//                + first + "&lastName=" + last;
//        HttpClient client = HttpClient.newHttpClient();
//        // HttpRequest ...
//        // get response ...
//        // convert the response to Java classes using a JSON library
//        // return the joke
//        return just the joke
//    }

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
