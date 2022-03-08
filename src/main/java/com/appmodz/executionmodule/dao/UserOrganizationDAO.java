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
public class UserOrganizationDAO {

    private final
    SessionFactory sessionFactory;

    @Autowired
    public UserOrganizationDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(UserOrganization userOrganization) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(userOrganization);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteByUserId(long userId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("delete from UserOrganization u where u.user.id = :userId")
                .setParameter("userId", userId);
        query.executeUpdate();
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteByUserIdAndOrganizationId(long userId, long organizationId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("delete from UserOrganization u where u.user.id = :userId and u.organization.organizationId = :organizationId")
                .setParameter("userId", userId)
                .setParameter("organizationId", organizationId);
        query.executeUpdate();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteByUserOrganizationId(long userOrganizationId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("delete from UserOrganization u where u.userOrganizationId= :userOrganizationId")
                .setParameter("userOrganizationId", userOrganizationId);
        query.executeUpdate();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getOrganizations(long userId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("SELECT u.organization from UserOrganization u where u.user.id = :userId")
                .setParameter("userId", userId);
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<Long> getOrganizationIds(long userId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("SELECT u.organization.organizationId from UserOrganization u where u.user.id = :userId")
                .setParameter("userId", userId);
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getUserOrganizations(long userId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from UserOrganization u where u.user.id = :userId")
                .setParameter("userId", userId);
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getUserOrganizationsByRole(long roleId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from UserOrganization u where u.role.roleId = :roleId")
                .setParameter("roleId", roleId);
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public UserOrganization getUserOrganization(long userOrganizationId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from UserOrganization u where u.userOrganizationId = :userOrganizationId")
                .setParameter("userOrganizationId", userOrganizationId);
        query.setMaxResults(1);
        return (UserOrganization) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Organization getOrganization(long userOrganizationId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("SELECT u.organization from UserOrganization u where u.userOrganizationId = :userOrganizationId")
                .setParameter("userOrganizationId", userOrganizationId);
        query.setMaxResults(1);
        return (Organization) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Long getCountOfUsersInOrganizations(List<Long> organizationIds) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("select count(distinct u.user) from UserOrganization u where u.organization.organizationId in (:organizationIds)")
                .setParameterList("organizationIds",organizationIds);
        return (Long)query.uniqueResult();
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Role getRole(long userOrganizationId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("SELECT u.role from UserOrganization u where u.userOrganizationId = :userOrganizationId")
                .setParameter("userOrganizationId", userOrganizationId);
        query.setMaxResults(1);
        return (Role) query.uniqueResult();
    }

}
