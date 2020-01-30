package com.oreilly.persistence.dao;

import com.oreilly.persistence.entities.Officer;
import com.oreilly.persistence.entities.Rank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
public class OfficerRepositoryTest {
    @Autowired
    private OfficerRepository repository;

    @Autowired
    private JdbcTemplate template;

    @Test
    public void testSave() {
        Officer officer = new Officer(Rank.LIEUTENANT, "Nyota", "Uhuru");
        officer = repository.save(officer);
        assertNotNull(officer.getId());
    }

    @Test
    public void findById() {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> {
                    Optional<Officer> officer = repository.findById(id);
                    assertTrue(officer.isPresent());
                    assertEquals(id, officer.get().getId());
                });
    }

    @Test
    public void findAll() {
        List<String> dbNames = repository.findAll().stream()
                                         .map(Officer::getLast)
                                         .collect(Collectors.toList());
        assertThat(dbNames, containsInAnyOrder("Kirk", "Picard", "Sisko", "Janeway", "Archer"));
    }

    @Test
    public void count() {
        assertEquals(5, repository.count());
    }

    @Test
    public void deleteById() {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> repository.deleteById(id));
        assertEquals(0, repository.count());
    }

    @Test
    public void existsById() {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> assertTrue(repository.existsById(id)));
    }

    @Test
    public void doesNotExist() {
        List<Integer> ids = template.query("select id from officers",
                                           (rs, num) -> rs.getInt("id"));
        assertThat(ids, not(contains(999)));
        assertFalse(repository.existsById(999));
    }

    @Test
    public void findByRank() {
        repository.findByRank(Rank.CAPTAIN).forEach(captain ->
                                                            assertEquals(Rank.CAPTAIN, captain.getRank()));

    }

    @Test
    public void findByLast() {
        List<Officer> kirks = repository.findByLast("Kirk");
        assertEquals(1, kirks.size());
        assertEquals("Kirk", kirks.get(0).getLast());
    }
}