package com.appmodz.executionmodule.dao;

import com.appmodz.executionmodule.model.License;
import com.appmodz.executionmodule.model.Organization;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class LicenseDAO {
    private final
    SessionFactory sessionFactory;

    @Autowired
    public LicenseDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(License license) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(license);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public License get(String licenseName) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from License where licenseName = :licenseName")
                .setParameter("licenseName",licenseName);
        query.setMaxResults(1);
        return (License) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public License get(long licenseId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from License where licenseId = :licenseId")
                .setParameter("licenseId",licenseId);
        query.setMaxResults(1);
        return (License) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getAll() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from License");
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getByRankDesc() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from License l order by l.licenseRank asc");
        return query.list();
    }
}
