package com.oreilly.dao;

import com.oreilly.entities.Officer;
import com.oreilly.entities.Rank;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class OfficerRepositoryTest {
    @Autowired
    private OfficerRepository dao;

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
    public void findOne() throws Exception {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> {
                    Officer officer = dao.findOne(id);
                    assertNotNull(officer);
                    assertEquals(id, officer.getId());
                });
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
        assertEquals(5, dao.count());
    }

    @Test
    public void delete() throws Exception {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> dao.delete(dao.findOne(id)));
        assertEquals(0, dao.count());
    }

    @Test
    public void exists() throws Exception {
        template.query("select id from officers", (rs, num) -> rs.getInt("id"))
                .forEach(id -> assertTrue(String.format("%d should exist", id),
                        dao.exists(id)));
    }

    @Test
    public void findByRank() throws Exception {
        dao.findByRank(Rank.CAPTAIN).forEach(captain ->
                assertEquals(Rank.CAPTAIN, captain.getRank()));

    }

    @Test
    public void findByLast() throws Exception {
        List<Officer> kirks = dao.findByLast("Kirk");
        assertEquals(1, kirks.size());
        assertEquals("Kirk", kirks.get(0).getLast());
    }
}