package com.appmodz.executionmodule.dao;

import com.appmodz.executionmodule.model.License;
import com.appmodz.executionmodule.model.ProductPackage;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class ProductPackageDAO {
    private final
    SessionFactory sessionFactory;

    @Autowired
    public ProductPackageDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(ProductPackage productPackage) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(productPackage);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public ProductPackage get(String productPackageName) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ProductPackage where productPackageName = :productPackageName")
                .setParameter("productPackageName",productPackageName);
        query.setMaxResults(1);
        return (ProductPackage) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public ProductPackage get(long productPackageId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ProductPackage where productPackageId = :productPackageId")
                .setParameter("productPackageId",productPackageId);
        query.setMaxResults(1);
        return (ProductPackage) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getAll() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ProductPackage");
        return query.list();
    }
}
