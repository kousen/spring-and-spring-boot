package com.oreilly.dao;

import com.oreilly.entities.Officer;

import java.util.Collection;
import java.util.Optional;

public interface OfficerDAO {
    Officer save(Officer officer);
    Optional<Officer> findOne(Integer id);
    Collection<Officer> findAll();
    Long count();
    void delete(Officer officer);
    boolean exists(Integer id);
}
