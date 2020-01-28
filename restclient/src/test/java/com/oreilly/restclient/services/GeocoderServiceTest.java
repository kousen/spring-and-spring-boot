package com.oreilly.restclient.services;

import com.oreilly.restclient.entities.Site;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class GeocoderServiceTest {
    private Logger logger = LoggerFactory.getLogger(GeocoderServiceTest.class);

    @Autowired
    private GeocoderService service;

    @Test
    public void getLatLngWithoutStreet() {
        Site site = service.getLatLng("Boston", "MA");
        logger.info(site.toString());
        assertAll(
                () -> assertEquals(42.36, site.getLatitude(), 0.01),
                () -> assertEquals(-71.06, site.getLongitude(), 0.01)
        );
    }

    @Test
    public void getLatLngWithStreet() throws Exception {
        Site site = service.getLatLng("1600 Ampitheatre Parkway",
                "Mountain View", "CA");
        logger.info(site.toString());
        assertAll(
                () -> assertEquals(37.42, site.getLatitude(), 0.01),
                () -> assertEquals(-122.08, site.getLongitude(), 0.01)
        );
    }
}