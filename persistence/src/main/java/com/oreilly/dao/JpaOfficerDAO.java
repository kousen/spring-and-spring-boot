package com.oreilly.dao;

import com.oreilly.entities.Officer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

@Repository @Transactional
public class JpaOfficerDAO implements OfficerDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Officer save(Officer officer) {
        entityManager.persist(officer);
        return officer;
    }

    @Override
    public Officer findOne(Integer id) {
        return entityManager.find(Officer.class, id);
    }

    @Override
    public Collection<Officer> findAll() {
        return entityManager.createQuery("select o from Officer o", Officer.class)
                .getResultList();
    }

    @Override
    public Long count() {
        return (Long) entityManager.createQuery("select count(o.id) from Officer o")
                .getSingleResult();
    }

    @Override
    public void delete(Officer officer) {
        entityManager.remove(officer);
    }

    @Override
    public boolean exists(Integer id) {
        Object result = entityManager.createQuery(
                "SELECT 1 from Officer o where o.id=:id")
                .setParameter("id", id)
                .getSingleResult();
        return result != null;
    }
}
