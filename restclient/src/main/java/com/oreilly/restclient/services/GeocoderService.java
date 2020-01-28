package com.oreilly.restclient.services;

import com.oreilly.restclient.entities.Site;
import com.oreilly.restclient.json.Response;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GeocoderService {
    private static final String KEY = "AIzaSyDw_d6dfxDEI7MAvqfGXEIsEMwjC1PWRno";

    private WebClient client;

    public GeocoderService(WebClient.Builder builder) {
        client = builder.baseUrl("https://maps.googleapis.com")
                .build();
    }

    private String encodeString(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Site getLatLng(String... address) {
        String encoded = Stream.of(address)
                .map(this::encodeString)
                .collect(Collectors.joining(","));
        String path = "/maps/api/geocode/json";
        Response response = client.get()
                .uri(uriBuilder ->
                        uriBuilder.path(path)
                        .queryParam("address", encoded)
                        .queryParam("key", KEY)
                        .build()
                )
                .retrieve()
                .bodyToMono(Response.class)
                .block(Duration.ofSeconds(2));
        assert response != null;
        return new Site(response.getFormattedAddress(),
                response.getLocation().getLat(),
                response.getLocation().getLng());
    }
}
