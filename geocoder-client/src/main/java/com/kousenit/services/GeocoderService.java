package com.kousenit.services;

import com.kousenit.entities.Response;
import com.kousenit.entities.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service @Transactional
public class GeocoderService {
    private static final String KEY = "AIzaSyDw_d6dfxDEI7MAvqfGXEIsEMwjC1PWRno";
    private static final Logger log = LoggerFactory.getLogger(GeocoderService.class);
    private static final String BASE = "https://maps.googleapis.com/maps/api/geocode/json";

    private final RestTemplate restTemplate;

    @Autowired
    public GeocoderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Site getLatLng(String... address) {
        String encodedAddress = Arrays.stream(address)
                .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8))
                .collect(Collectors.joining(","));
        String url = String.format("%s?address=%s&key=%s", BASE, encodedAddress, KEY);
        Response response = restTemplate.getForObject(url, Response.class);
        log.info(String.format("Lat/Lng for %s: %s",
                response.getFormattedAddress(), response.getLocation()));
        return new Site(response.getFormattedAddress(),
                response.getLocation().getLat(),
                response.getLocation().getLng());
    }

}
