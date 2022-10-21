package com.oreilly.astro.services;

import com.oreilly.astro.entities.AstroResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
public class AstroService {

    private final RestTemplate template;
    private final WebClient client;

    @Autowired
    public AstroService(RestTemplateBuilder builder, WebClient.Builder webClientBuilder) {
        template = builder.setConnectTimeout(Duration.ofSeconds(3)).build();
        client = webClientBuilder.baseUrl("http://api.open-notify.org").build();
    }

    public AstroResponse getAstronauts() {
        String url = "http://api.open-notify.org/astros.json";
        return template.getForObject(url, AstroResponse.class);
    }

    public AstroResponse getAstronautsReactive() {
        return client.get()
                .uri("/astros.json")
                .retrieve()
                .bodyToMono(AstroResponse.class)
                .block(Duration.ofSeconds(2));
    }
}
