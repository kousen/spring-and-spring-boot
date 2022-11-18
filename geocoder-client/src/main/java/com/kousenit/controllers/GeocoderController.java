package com.kousenit.controllers;

import com.kousenit.entities.Site;
import com.kousenit.repositories.SiteRepository;
import com.kousenit.services.GeocoderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GeocoderController {
    private final GeocoderService service;
    private final SiteRepository repository;

    public GeocoderController(GeocoderService service, SiteRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping("/geo")
    public Site getLocation(@RequestParam(name = "city", required = false) String city,
                            @RequestParam(name = "state", required = false) String state) {
        String c = city;
        String s = state;
        if (city == null || city.length() == 0) c = "Boston";
        if (state == null || state.length() == 0) s = "MA";
        return service.getLatLng(c, s);
    }

    @GetMapping("/geo/{id}")
    public ResponseEntity<Site> getLocation(@PathVariable Integer id) {
        return ResponseEntity.of(repository.findById(id));
    }

    @GetMapping("/all")
    public List<Site> getAllLocations() {
        return repository.findAll();
    }

    @PostMapping("/geo")
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@RequestParam(name = "city") String city,
                     @RequestParam(name = "state") String state) {
        repository.save(getLocation(city, state));
    }
}
