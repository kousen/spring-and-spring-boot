package com.oreilly.persistence.dao;

import com.oreilly.persistence.entities.Officer;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("JpaQlInspection")
@Repository
public class JpaOfficerDAO implements OfficerDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Officer save(Officer officer) {
        entityManager.persist(officer);
        return officer;
    }

    @Override
    public Optional<Officer> findById(Integer id) {
        return Optional.ofNullable(entityManager.find(Officer.class, id));
    }

    @Override
    public List<Officer> findAll() {
        return entityManager.createQuery("select o from Officer o", Officer.class)
                            .getResultList();
    }

    @Override
    public long count() {
        return entityManager.createQuery("select count(o.id) from Officer o", Long.class)
                            .getSingleResult();
    }

    @Override
    public void delete(Officer officer) {
        entityManager.remove(officer);
    }

    @Override
    public boolean existsById(Integer id) {
        Object result = entityManager.createQuery(
                "SELECT 1 from Officer o where o.id=:id")
                                     .setParameter("id", id)
                                     .getSingleResult();
        return result != null;
    }
}
