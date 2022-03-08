package com.appmodz.executionmodule.dao;

import com.appmodz.executionmodule.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class PropertyDAO {

    private final
    SessionFactory sessionFactory;

    @Autowired
    public PropertyDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(Property property) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(property);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(PropertyL2 property) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(property);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(PropertyL3 property) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(property);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(PropertyL4 property) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(property);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(PropertyL5 property) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(property);
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Property property) {
        this.sessionFactory
                .getCurrentSession().delete(property);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(PropertyL2 property) {
        this.sessionFactory
                .getCurrentSession().delete(property);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(PropertyL3 property) {
        this.sessionFactory
                .getCurrentSession().delete(property);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(PropertyL4 property) {
        this.sessionFactory
                .getCurrentSession().delete(property);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(PropertyL5 property) {
        this.sessionFactory
                .getCurrentSession().delete(property);
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Property get(long id) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from Property where id = :id")
                .setParameter("id",id);
        query.setMaxResults(1);
        return (Property) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public PropertyL2 getL2(long id) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from PropertyL2 where id = :id")
                .setParameter("id",id);
        query.setMaxResults(1);
        return (PropertyL2) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public PropertyL3 getL3(long id) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from PropertyL3 where id = :id")
                .setParameter("id",id);
        query.setMaxResults(1);
        return (PropertyL3) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public PropertyL4 getL4(long id) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from PropertyL4 where id = :id")
                .setParameter("id",id);
        query.setMaxResults(1);
        return (PropertyL4) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public PropertyL5 getL5(long id) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from PropertyL5 where id = :id")
                .setParameter("id",id);
        query.setMaxResults(1);
        return (PropertyL5) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Property getByName(String name) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from Property where name = :name")
                .setParameter("name",name);
        query.setMaxResults(1);
        return (Property) query.uniqueResult();
    }

}
