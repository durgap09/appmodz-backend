package com.appmodz.executionmodule.dao;

import com.appmodz.executionmodule.model.Stack;
import com.appmodz.executionmodule.model.Template;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class TemplateDAO {

    private final
    SessionFactory sessionFactory;

    @Autowired
    public TemplateDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(Template template) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(template);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Template get(long id) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from Template where id = :id")
                .setParameter("id",id);
        query.setMaxResults(1);
        return (Template) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void updateUserToNull(long userId) {
        this.sessionFactory
                .getCurrentSession()
                .createQuery("update Template t set t.owner=null where t.owner.userId = :id")
                .setParameter("id",userId)
                .executeUpdate();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Template template) {
        this.sessionFactory
                .getCurrentSession().delete(template);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getAll() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from Template");
        return query.list();
    }

}
