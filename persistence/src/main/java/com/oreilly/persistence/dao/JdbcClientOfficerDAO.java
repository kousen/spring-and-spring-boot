package com.oreilly.persistence.dao;

import com.oreilly.persistence.entities.Officer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"ConstantConditions", "SqlResolve", "SqlNoDataSourceInspection"})
@Repository
public class JdbcClientOfficerDAO implements OfficerDAO {
    private final JdbcClient jdbcClient;
    private final SimpleJdbcInsert insertOfficer;

    @Autowired
    public JdbcClientOfficerDAO(JdbcTemplate jdbcTemplate, JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
        insertOfficer = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("officers")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Officer save(Officer officer) {
        SqlParameterSource source = new BeanPropertySqlParameterSource(officer);
        Integer newId = (Integer) insertOfficer.executeAndReturnKey(source);
        officer.setId(newId);
        return officer;
    }

    @Override
    public Optional<Officer> findById(Integer id) {
        return jdbcClient.sql("select * from officers where id=?")
                .param(id)
                .query(Officer.class)
                .optional();
    }

    @Override
    public List<Officer> findAll() {
        return jdbcClient.sql("SELECT * FROM officers")
                .query(Officer.class)
                .list();
    }

    @Override
    public long count() {
        return jdbcClient.sql("select count(*) from officers")
                .query(Long.class)
                .single();
    }

    @Override
    public void delete(Officer officer) {
        jdbcClient.sql("DELETE FROM officers WHERE id=?")
                .param(officer.getId())
                .update();
    }

    @Override
    public boolean existsById(Integer id) {
        return jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM officers where id=?)")
                .param(id)
                .query(Boolean.class)
                .single();
    }
}
