package com.appmodz.executionmodule.dao;

import com.appmodz.executionmodule.model.ApplicationLog;
import com.appmodz.executionmodule.model.Permission;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

@Repository
public class ApplicationLogDAO {

    private final
    SessionFactory sessionFactory;

    @Autowired
    public ApplicationLogDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(ApplicationLog applicationLog) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(applicationLog);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(ApplicationLog applicationLog) {
        this.sessionFactory
                .getCurrentSession().delete(applicationLog);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getAll() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ApplicationLog");
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteBefore7Days() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        Query query = this.sessionFactory
                .getCurrentSession().createQuery("delete from ApplicationLog a where a.time<:date")
                .setParameter("date",cal.getTime());
        query.executeUpdate();
    }


}
