package com.kousenit.restclient.services;

import com.kousenit.restclient.entities.Site;
import com.kousenit.restclient.json.Response;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class GeocoderService {
    private static final String KEY = "AIzaSyDz89mFBP4a-N8jE7uFQEd1J8y7d-_ksH4";

    private final WebClient client;

    public GeocoderService() {
        client = WebClient.create("https://maps.googleapis.com");
    }

    private String encodeString(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public Site getLatLng(String... address) {
        String encoded = Arrays.stream(address)
                .map(this::encodeString)
                .collect(Collectors.joining(","));
        String path = "/maps/api/geocode/json";
        Response response = client.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("address", encoded)
                        .queryParam("key", KEY)
                        .build()
                )
                .retrieve()
                .bodyToMono(Response.class)
                .log()
                .block(Duration.ofSeconds(2));
        assert response != null;
        return new Site(response.getFormattedAddress(),
                response.getLocation().getLat(),
                response.getLocation().getLng());
    }
}
