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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection"})
@DataJpaTest
@Transactional
public class OfficerRepositoryTest {
    @Autowired
    private OfficerRepository repository;

    @Autowired
    private JdbcTemplate template;

    private List<Integer> getIds() {
        return template.query("select id from officers", (rs, num) -> rs.getInt("id"));
    }

    @Test
    public void testSave() {
        Officer officer = new Officer(Rank.LIEUTENANT, "Nyota", "Uhuru");
        officer = repository.save(officer);
        assertNotNull(officer.getId());
    }

    @Test
    public void findById() {
        getIds().forEach(id -> {
                    Optional<Officer> officer = repository.findById(id);
                    assertTrue(officer.isPresent());
                    assertEquals(id, officer.get().getId());
                });
    }

    @Test
    public void findAll() {
        List<String> dbNames = repository.findAll()
                .stream()
                .map(Officer::getLastName)
                .collect(Collectors.toList());
        assertThat(dbNames).contains("Kirk", "Picard", "Sisko", "Janeway", "Archer");
    }

    @Test
    public void count() {
        assertEquals(5, repository.count());
    }

    @Test
    public void deleteById() {
        getIds().forEach(id -> repository.deleteById(id));
        assertEquals(0, repository.count());
    }

    @Test
    public void existsById() {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> assertTrue(repository.existsById(id)));
    }

    @Test
    public void doesNotExist() {
        assertThat(getIds()).doesNotContain(999);
        assertFalse(repository.existsById(999));
    }

    @Test
    public void findByRank() {
        repository.findByRank(Rank.CAPTAIN)
                .forEach(captain ->
                        assertEquals(Rank.CAPTAIN, captain.getRank()));

    }

    @Test
    public void findByLastNameLikeAndRank() {
        List<Officer> officers = repository.findByLastNameLikeAndRank("%i%", Rank.CAPTAIN);
        assertEquals(3, officers.size());
        officers.forEach(officer -> assertTrue(officer.getLastName().contains("i")));
        officers.forEach(System.out::println);
    }
}