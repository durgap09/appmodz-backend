package com.appmodz.executionmodule.dao;

import com.appmodz.executionmodule.dto.CanvasRequestDTO;
import com.appmodz.executionmodule.dto.SearchRequestDTO;
import com.appmodz.executionmodule.dto.SearchResultDTO;
import com.appmodz.executionmodule.model.*;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class ComponentDAO {

    private final
    SessionFactory sessionFactory;

    @Autowired
    public ComponentDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(Component component) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(component);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(AppmodzCategory appmodzCategory) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(appmodzCategory);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(AppmodzComponentCategory appmodzComponentCategory) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(appmodzComponentCategory);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(ComponentCategory componentCategory) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(componentCategory);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Component get(long id) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from Component where id = :id")
                .setParameter("id",id);
        query.setMaxResults(1);
        return (Component) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Component component) {
        this.sessionFactory
                .getCurrentSession().delete(component);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(AppmodzCategory appmodzCategory) {
        this.sessionFactory
                .getCurrentSession().delete(appmodzCategory);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(ComponentCategory componentCategory) {
        this.sessionFactory
                .getCurrentSession().delete(componentCategory);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Component get(String name, long componentCategoryId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from Component c where c.name = :name and c.componentCategory.componentCategoryId = :componentCategoryId")
                .setParameter("name",name)
                .setParameter("componentCategoryId",componentCategoryId);
        query.setMaxResults(1);
        return (Component) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public CloudPlatform getCloudPlatform(long id) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from CloudPlatform where cloudPlatformId = :id")
                .setParameter("id",id);
        query.setMaxResults(1);
        return (CloudPlatform) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public CloudPlatform getCloudPlatform(String cloudPlatformName) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from CloudPlatform where cloudPlatformName = :cloudPlatformName")
                .setParameter("cloudPlatformName",cloudPlatformName);
        query.setMaxResults(1);
        return (CloudPlatform) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public ComponentCategory getComponentCategory(long id) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ComponentCategory where id = :id")
                .setParameter("id",id);
        query.setMaxResults(1);
        return (ComponentCategory) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public ComponentCategory getComponentCategory(String componentCategoryName, String cloudPlatformName) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ComponentCategory c where c.componentCategoryName = :componentCategoryName and c.cloudPlatform.cloudPlatformName = :cloudPlatformName")
                .setParameter("componentCategoryName",componentCategoryName)
                .setParameter("cloudPlatformName",cloudPlatformName);
        query.setMaxResults(1);
        return (ComponentCategory) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getCloudPlatforms() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from CloudPlatform");
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getAppmodzCategories() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from AppmodzCategory");
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public AppmodzComponentCategory getAppmodzComponentCategoryByAppmodzCategoryId(long appmodzCategotyId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from AppmodzComponentCategory a  where a.appmodzCategory.id=:appmodzCategotyId")
                .setParameter("appmodzCategotyId",appmodzCategotyId);
        query.setMaxResults(1);
        return (AppmodzComponentCategory) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getComponentsByCategoryId(long appmodzCategotyId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("select a.components from AppmodzComponentCategory a where a.appmodzCategory.id=:appmodzCategotyId")
                .setParameter("appmodzCategotyId",appmodzCategotyId);
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getCloudPlatformConfigParameters(long cloudPlatformId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from CloudPlatformConfigParameter cp where cp.cloudPlatform.cloudPlatformId= :cloudPlatformId")
                .setParameter("cloudPlatformId",cloudPlatformId);;
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public SearchResultDTO search(SearchRequestDTO searchRequestDTO) {
        Long count = (Long) this.sessionFactory
                .getCurrentSession().createQuery("select count(*) from Component").uniqueResult();

        CriteriaBuilder builder = this.sessionFactory
                .getCurrentSession().getCriteriaBuilder();

        CriteriaQuery<Component> criteriaQuery = builder.createQuery(Component.class);
        Root<Component> root = criteriaQuery.from(Component.class);
        criteriaQuery = criteriaQuery.where(
                builder.or(
                        builder.like(builder.lower(root.get("name")),
                                "%"+ searchRequestDTO.getSearch().toLowerCase()+"%"),
                        builder.like(builder.lower(root.get("appmodzName")),
                                "%"+ searchRequestDTO.getSearch().toLowerCase()+"%"))
        );
        List<Order> orderList = new ArrayList();
        if (searchRequestDTO.getSort().getSort().equals("desc")) {
            orderList.add(builder.desc(root.get(searchRequestDTO.getSort().getAttribute())));
        } else {
            orderList.add(builder.asc(root.get(searchRequestDTO.getSort().getAttribute())));
        }
        criteriaQuery = criteriaQuery.orderBy(orderList);
        Query query = this.sessionFactory.getCurrentSession().createQuery(criteriaQuery);
        ScrollableResults scrollable = query.scroll(ScrollMode.SCROLL_INSENSITIVE);
        SearchResultDTO searchResultDTO = new SearchResultDTO();
        if(scrollable.last()) {
            searchResultDTO.setTotal(count);
            query.setFirstResult((searchRequestDTO.getPageNo()-1)* searchRequestDTO.getItemPerPage())
                    .setMaxResults(searchRequestDTO.getItemPerPage());
            searchResultDTO.setData(Collections.unmodifiableList(query.list()));
        }
        scrollable.close();
        return searchResultDTO;
    }
}
