package com.oreilly.persistence.dao;

import com.oreilly.persistence.entities.Officer;
import com.oreilly.persistence.entities.Rank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings({"ConstantConditions", "SqlResolve", "SqlNoDataSourceInspection"})
@Repository
public class JdbcClientOfficerDAO implements OfficerDAO {
    private final JdbcClient jdbcClient;
    private final SimpleJdbcInsert insertOfficer;

    private final RowMapper<Officer> officerMapper =
            (ResultSet rs, int rowNum) -> new Officer(rs.getInt("id"),
                    Rank.valueOf(rs.getString("rank")),
                    rs.getString("first_name"),
                    rs.getString("last_name"));

    @Autowired
    public JdbcClientOfficerDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcClient = JdbcClient.create(jdbcTemplate);
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
        try (Stream<Officer> stream =
                     jdbcClient.sql("select * from officers where id=?")
                             .param(id)
                             .query(officerMapper)
                             .stream()) {
            return stream.findAny();
        }
    }

    // Alternative 1: extra SQL call to verify row exists
    @SuppressWarnings("unused")
    public Optional<Officer> findById1(Integer id) {
        if (!existsById(id)) return Optional.empty();
        return jdbcClient.sql("SELECT * FROM officers WHERE id=?")
                .param(id)
                .query(officerMapper)
                .optional();
    }

    // Alternative 2: catch the exception when row doesn't exist
    @SuppressWarnings("unused")
    public Optional<Officer> findById2(Integer id) {
        try {
            return jdbcClient.sql("SELECT * FROM officers WHERE id=?")
                    .param(id)
                    .query(officerMapper)
                    .optional();
        } catch (IncorrectResultSizeDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Officer> findAll() {
        return jdbcClient.sql("SELECT * FROM officers")
                .query(officerMapper)
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
