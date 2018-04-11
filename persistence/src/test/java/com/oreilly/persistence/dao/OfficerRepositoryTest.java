package com.oreilly.persistence.dao;

import com.oreilly.persistence.entities.Officer;
import com.oreilly.persistence.entities.Rank;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

@DataJpaTest
@RunWith(SpringRunner.class)
@Transactional
public class OfficerRepositoryTest {
    @Autowired
    private OfficerRepository repository;

    @Autowired
    private JdbcTemplate template;

    @Test
    public void testSave() throws Exception {
        Officer officer = new Officer(Rank.LIEUTENANT, "Nyota", "Uhuru");
        officer = repository.save(officer);
        assertNotNull(officer.getId());
    }

    @Test
    public void findById() throws Exception {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> {
                    Optional<Officer> officer = repository.findById(id);
                    assertTrue(officer.isPresent());
                    assertEquals(id, officer.get().getId());
                });
    }

    @Test
    public void findAll() throws Exception {
        List<String> dbNames = repository.findAll().stream()
                                         .map(Officer::getLast)
                                         .collect(Collectors.toList());
        assertThat(dbNames, containsInAnyOrder("Kirk", "Picard", "Sisko", "Janeway", "Archer"));
    }

    @Test
    public void count() throws Exception {
        assertEquals(5, repository.count());
    }

    @Test
    public void deleteById() throws Exception {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> repository.deleteById(id));
        assertEquals(0, repository.count());
    }

    @Test
    public void existsById() throws Exception {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> assertTrue(String.format("%d should exist", id),
                                          repository.existsById(id)));
    }

    @Test
    public void findByRank() throws Exception {
        repository.findByRank(Rank.CAPTAIN).forEach(captain ->
                                                            assertEquals(Rank.CAPTAIN, captain.getRank()));

    }

    @Test
    public void findByLast() throws Exception {
        List<Officer> kirks = repository.findByLast("Kirk");
        assertEquals(1, kirks.size());
        assertEquals("Kirk", kirks.get(0).getLast());
    }
}