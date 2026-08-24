package com.kousenit.restclient.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.kousenit.restclient.json.LaunchLibraryRecords.*;

@Service
public class LaunchLibraryService {
    private static final String BASE_URL = "https://ll.thespacedevs.com";
    private final RestClient client;

    public LaunchLibraryService() {
        this.client = RestClient.builder().baseUrl(BASE_URL).build();
    }

    public List<Expedition> getExpeditions() {
        return Objects.requireNonNull(client.get()
                        .uri("/2.3.0/expeditions/?is_active=true&mode=detailed")
                        .retrieve()
                        .body(ExpeditionResponse.class))
                .results();
    }

    public List<AstronautAssignment> getAstronautAssignments() {
        return getExpeditions().stream()
                .flatMap(expedition -> expedition.crew().stream()
                        .map(member -> new AstronautAssignment(
                                member.astronaut().name(),
                                member.role().role(),
                                member.astronaut().agency().abbrev(),
                                expedition.spacestation().name()
                        )))
                .toList();
    }

    public Map<String, Long> getCrewCountByStation() {
        return getExpeditions().stream()
                .collect(Collectors.groupingBy(
                        exp -> exp.spacestation().name(),
                        Collectors.summingLong(exp -> exp.crew().size())
                ));
    }
}
