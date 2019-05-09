package com.oreilly.astro.services;

import com.oreilly.astro.entities.AstroResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
public class AstroService {

    private RestTemplate template;

    @Autowired
    public AstroService(RestTemplateBuilder builder) {
        template = builder.setConnectTimeout(Duration.ofSeconds(3)).build();
    }

    public AstroResponse getAstronauts() {
        String url = "http://api.open-notify.org/astros.json";
        System.out.println("Going over the internet now");
        return template.getForObject(url, AstroResponse.class);
    }
}
