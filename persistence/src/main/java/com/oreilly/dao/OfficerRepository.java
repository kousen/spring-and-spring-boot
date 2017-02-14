package com.oreilly.dao;

import com.oreilly.entities.Officer;
import com.oreilly.entities.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OfficerRepository extends JpaRepository<Officer,Integer> {
    List<Officer> findByLast(@Param("last") String last);
    List<Officer> findByRank(@Param("rank") Rank rank);
}
