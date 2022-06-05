package com.oreilly.persistence.dao;

import com.oreilly.persistence.entities.Rank;
import com.oreilly.persistence.entities.ReactiveOfficer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
@SpringBootTest
//@Transactional
public class OfficerReactiveRepositoryTest {
    @Autowired
    private OfficerReactiveRepository repository;

    @Autowired
    private DatabaseClient databaseClient;

    private Stream<Integer> getIds() {
        return databaseClient.sql("select id from officers")
                .map(row -> row.get("id", Integer.class))
                .all()
                .toStream();
        }

    @Test
    public void testSave() {
        ReactiveOfficer officer = new ReactiveOfficer(Rank.LIEUTENANT, "Nyota", "Uhuru");
        officer = repository.save(officer).block();
        assertNotNull(officer.getId());
    }

    @Test
    public void findById() {
        getIds().forEach(id -> {
                    Optional<ReactiveOfficer> officer = repository.findById(id).blockOptional();
                    assertTrue(officer.isPresent());
                    assertEquals(id, officer.get().getId());
                });
    }

    @Test
    public void findAll() {
        List<String> dbNames = repository.findAll().toStream()
                .map(ReactiveOfficer::getLastName)
                .collect(Collectors.toList());
        assertThat(dbNames).contains("Kirk", "Picard", "Sisko", "Janeway", "Archer");
    }

    @Test
    public void count() {
        assertEquals(5, repository.count().block());
    }

    @Test
    public void deleteById() {
        getIds().forEach(id -> repository.deleteById(id).block());
        assertEquals(0, repository.count().block());
    }

    @Test
    public void existsById() {
        getIds().forEach(id -> assertEquals(Boolean.TRUE, repository.existsById(id).block()));
    }

    @Test
    public void doesNotExist() {
        assertThat(getIds()).doesNotContain(999);
        assertNotEquals(Boolean.TRUE, repository.existsById(999).block());
    }

    @Test
    public void findByRank() {
        repository.findByRank(Rank.CAPTAIN).toStream()
                .forEach(captain ->
                        assertEquals(Rank.CAPTAIN, captain.getRank()));

    }

    @Test
    public void findByLastNameLikeAndRank() {
        Iterable<ReactiveOfficer> officers = repository.findByLastNameLikeAndRank("%i%", Rank.CAPTAIN).toIterable();
        officers.forEach(officer -> assertTrue(officer.getLastName().contains("i")));
        officers.forEach(System.out::println);
    }
}