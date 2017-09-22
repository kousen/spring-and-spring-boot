package com.oreilly.dao;

import com.oreilly.entities.Officer;
import com.oreilly.entities.Rank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Optional;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
@Repository
public class JdbcOfficerDAO implements OfficerDAO {
    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert insertOfficer;

    @Autowired
    public JdbcOfficerDAO(@Qualifier("dataSource") DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
        insertOfficer = new SimpleJdbcInsert(dataSource)
                .withTableName("officers")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Officer save(Officer officer) {
//        SqlParameterSource parameters = new BeanPropertySqlParameterSource(officer);
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("rank", officer.getRank())
                .addValue("first_name", officer.getFirst())
                .addValue("last_name", officer.getLast());
        Integer newId = (Integer) insertOfficer.executeAndReturnKey(parameters);
        officer.setId(newId);
        return officer;
    }

    @Override
    public Optional<Officer> findOne(Integer id) {
        if (!exists(id)) return Optional.empty();
        return Optional.of(jdbcTemplate.queryForObject(
                "SELECT * FROM officers WHERE id=?",
                mapper,
                id));
    }

    private RowMapper<Officer> mapper = (rs, rowNum) -> new Officer(rs.getInt("id"), // Java 8 lambda expression
            Rank.valueOf(rs.getString("rank")),
            rs.getString("first_name"),
            rs.getString("last_name"));

    @Override
    public Collection<Officer> findAll() {
        return jdbcTemplate.query("SELECT * FROM officers", mapper);
    }

    @Override
    public Long count() {
        return jdbcTemplate.queryForObject(
                "select count(*) from officers", Long.class);
    }

    @Override
    public void delete(Officer officer) {
        jdbcTemplate.update("DELETE FROM officers WHERE id=?", officer.getId());
    }

    @Override
    public boolean exists(Integer id) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM officers where id=?)", Boolean.class, id);
    }
}
