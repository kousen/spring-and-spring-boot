package com.kousenit.services;

import com.kousenit.entities.Location;
import com.kousenit.entities.Response;
import com.kousenit.entities.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service @Transactional
public class GeocoderService {
    private static final Logger log = LoggerFactory.getLogger(GeocoderService.class);
    private static final String BASE = "https://maps.googleapis.com/maps/api/geocode/json";

    private RestTemplate restTemplate;

    @Autowired
    public GeocoderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private String encodeString(String s) {
        try {
            return URLEncoder.encode(s,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public Site getLatLng(String... address) {
        String encodedAddress = Stream.of(address)
                .map(this::encodeString)
                .collect(Collectors.joining(","));
        String url = String.format("%s?address=%s", BASE, encodedAddress);
        Response response = restTemplate.getForObject(url, Response.class);
        log.info(String.format("Lat/Lng for %s: %s",
                response.getFormattedAddress(), response.getLocation()));
        return new Site(response.getFormattedAddress(),
                response.getLocation().getLat(),
                response.getLocation().getLng());
    }

}
