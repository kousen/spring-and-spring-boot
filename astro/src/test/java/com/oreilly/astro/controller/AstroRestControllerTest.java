package com.oreilly.astro.controller;

import com.oreilly.astro.entities.AstroResponse;
import com.oreilly.astro.services.AstroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AstroRestControllerTest {
    @Autowired
    private TestRestTemplate template;

    @MockBean
    private AstroService service;

    @Test
    public void numberOfAstronauts() {
        AstroResponse astroResponse = new AstroResponse();
        astroResponse.setNumber(6);
        // Mockito, setting expectations:
        //given(service.getAstronauts()).willReturn(astroResponse);
        when(service.getAstronauts()).thenReturn(astroResponse);

        String response = template.getForObject("/astronauts", String.class);
        assertEquals("There are 6 people in space", response);
    }
}