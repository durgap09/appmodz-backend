package com.appmodz.executionmodule.dao;

import com.appmodz.executionmodule.model.ApplicationConfiguration;
import com.appmodz.executionmodule.model.Component;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ApplicationConfigurationDAO {
    private final
    SessionFactory sessionFactory;

    @Autowired
    public ApplicationConfigurationDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(ApplicationConfiguration applicationConfiguration) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(applicationConfiguration);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public ApplicationConfiguration get(long id) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ApplicationConfiguration where id = :id")
                .setParameter("id",id);
        query.setMaxResults(1);
        return (ApplicationConfiguration) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public ApplicationConfiguration getByKey(String key) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ApplicationConfiguration where key = :key")
                .setParameter("key",key);
        query.setMaxResults(1);
        return (ApplicationConfiguration) query.uniqueResult();
    }
}
