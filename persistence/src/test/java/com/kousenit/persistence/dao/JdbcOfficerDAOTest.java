package com.kousenit.persistence.dao;

import com.kousenit.persistence.entities.Officer;
import com.kousenit.persistence.entities.Rank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // tx for each test rolls back by default
public class JdbcOfficerDAOTest {
    @Qualifier("jdbcOfficerDAO")
    @Autowired
    private OfficerDAO dao;

    @Test
    public void save() {
        Officer officer = new Officer(Rank.LIEUTENANT, "Nyota", "Uhuru");
        officer = dao.save(officer);
        assertNotNull(officer.getId());
    }

    @Test
    public void findByIdThatExists() {
        Optional<Officer> officer = dao.findById(1);
        assertTrue(officer.isPresent());
        assertEquals(1, officer.get().getId().intValue());
    }

    @Test
    public void findByIdThatDoesNotExist() {
        Optional<Officer> officer = dao.findById(999);
        assertFalse(officer.isPresent());
    }

    @Test
    public void count() {
        assertEquals(5, dao.count());
    }

    @Test
    public void findAll() {
        List<String> dbNames = dao.findAll().stream()
                                  .map(Officer::getLastName)
                                  .collect(Collectors.toList());
        assertThat(dbNames).contains("Kirk", "Picard", "Sisko", "Janeway", "Archer");
    }

    @Test
    public void delete() {
        IntStream.rangeClosed(1, 5)
                 .forEach(id -> {
                     Optional<Officer> officer = dao.findById(id);
                     assertTrue(officer.isPresent());
                     dao.delete(officer.get());
                 });
        assertEquals(0, dao.count());
    }

    @Test
    public void existsById() {
        IntStream.rangeClosed(1, 5)
                 .forEach(id -> assertTrue(dao.existsById(id)));
    }
}