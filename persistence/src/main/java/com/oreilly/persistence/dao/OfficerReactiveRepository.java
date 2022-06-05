package com.oreilly.persistence.dao;

import com.oreilly.persistence.entities.Rank;
import com.oreilly.persistence.entities.ReactiveOfficer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OfficerReactiveRepository extends ReactiveCrudRepository<ReactiveOfficer, Integer> {
    Flux<ReactiveOfficer> findByRank(Rank rank);
    Flux<ReactiveOfficer> findByLastNameLikeAndRank(String like, Rank rank);
}

