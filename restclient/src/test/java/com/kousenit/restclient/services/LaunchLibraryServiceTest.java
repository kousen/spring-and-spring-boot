package com.kousenit.restclient.services;

import com.kousenit.restclient.json.LaunchLibraryRecords.AstronautAssignment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LaunchLibraryServiceTest {
    @Autowired
    private LaunchLibraryService service;

    @Test
    void expeditions_have_crew_aboard_stations() {
        var expeditions = service.getExpeditions();

        assertThat(expeditions)
                .isNotEmpty()
                .allSatisfy(expedition -> {
            assertThat(expedition.spacestation()).isNotNull();
            assertThat(expedition.spacestation().name()).isNotBlank();
            assertThat(expedition.crew()).isNotEmpty();
        });

        // Print the astronauts and what station they are aboard
        expeditions.forEach(expedition -> {
            String stationName = expedition.spacestation().name();
            expedition.crew().forEach(member -> {
                String astronautName = member.astronaut().name();
                System.out.printf("%s is aboard the %s%n", astronautName, stationName);
            });
        });
    }

    @Test
    void astronaut_assignments_have_required_fields() {
        List<AstronautAssignment> assignments = service.getAstronautAssignments();

        assertThat(assignments)
                .isNotEmpty()
                .allSatisfy(assignment -> {
            assertThat(assignment.astronautName()).isNotBlank();
            assertThat(assignment.role()).isNotBlank();
            assertThat(assignment.agency()).isNotBlank();
            assertThat(assignment.stationName()).isNotBlank();
        });
    }

    @Test
    void crew_count_by_station_returns_positive_counts() {
        Map<String, Long> crewCounts = service.getCrewCountByStation();

        assertThat(crewCounts).isNotEmpty();
        assertThat(crewCounts.values()).allSatisfy(count ->
                assertThat(count).isPositive()
        );

        // Print the crew counts for visual verification
        crewCounts.forEach((station, count) ->
                System.out.printf("There are %d astronauts aboard the %s%n", count, station));
    }
}