package com.kousenit.restclient.services;

import com.kousenit.restclient.json.AstroResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
public class AstroService {
    private final WebClient client;
    private final RestTemplate template;
    private final String baseUrl = "http://api.open-notify.org";

    @Autowired
    public AstroService(RestTemplateBuilder restTemplateBuilder) {
        client = WebClient.create(baseUrl);
        template = restTemplateBuilder.build();
    }

    public AstroResponse getAstroResponseRT() {
        String url = baseUrl + "/astros.json";
        return template.getForObject(url, AstroResponse.class);
    }

    public AstroResponse getAstroResponse() {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/astros.json")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AstroResponse.class)
                .block(Duration.ofSeconds(2));
    }
}
