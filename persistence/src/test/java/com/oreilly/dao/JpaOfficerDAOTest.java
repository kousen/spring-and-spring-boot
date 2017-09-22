package com.oreilly.dao;

import com.oreilly.entities.Officer;
import com.oreilly.entities.Rank;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class JpaOfficerDAOTest {
    @Autowired @Qualifier("jpaOfficerDAO")
    private OfficerDAO dao;

    @Autowired
    private JdbcTemplate template;

    @Test
    public void testSave() throws Exception {
        Officer officer = new Officer(Rank.LIEUTENANT, "Nyota", "Uhuru");
        assertNull(officer.getId());
        officer = dao.save(officer);
        assertNotNull(officer.getId());
    }

    @Test
    public void findOneThatExists() throws Exception {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> {
                    Optional<Officer> officer = dao.findOne(id);
                    assertTrue(officer.isPresent());
                    assertEquals(id, officer.get().getId());
                });
    }

    @Test
    public void findOneThatDoesNotExist() throws Exception {
        Optional<Officer> officer = dao.findOne(999);
        assertFalse(officer.isPresent());
    }

    @Test
    public void findAll() throws Exception {
        List<String> dbNames = dao.findAll().stream()
                .map(Officer::getLast)
                .collect(Collectors.toList());
        assertThat(dbNames, containsInAnyOrder("Kirk", "Picard", "Sisko", "Janeway", "Archer"));
    }

    @Test
    public void count() throws Exception {
        assertEquals(5, dao.count().intValue());
    }

    @Test
    public void delete() throws Exception {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> {
                    Optional<Officer> officer = dao.findOne(id);
                    assertTrue(officer.isPresent());
                    dao.delete(officer.get());
                });
        assertEquals(0, dao.count().intValue());
    }

    @Test
    public void exists() throws Exception {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> assertTrue(String.format("%d should exist", id),
                        dao.exists(id)));
    }
}
