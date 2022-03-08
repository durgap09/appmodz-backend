package com.appmodz.executionmodule.dao;

import com.appmodz.executionmodule.dto.SearchRequestDTO;
import com.appmodz.executionmodule.dto.SearchResultDTO;
import com.appmodz.executionmodule.model.Component;
import com.appmodz.executionmodule.model.ConfigurationProfile;
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
public class ConfigurationProfileDAO {
    private final
    SessionFactory sessionFactory;

    @Autowired
    public ConfigurationProfileDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(ConfigurationProfile configurationProfile) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(configurationProfile);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public ConfigurationProfile get(long id) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ConfigurationProfile where id = :id")
                .setParameter("id",id);
        query.setMaxResults(1);
        return (ConfigurationProfile) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public ConfigurationProfile getByName(String name) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ConfigurationProfile where name = :name")
                .setParameter("name",name);
        query.setMaxResults(1);
        return (ConfigurationProfile) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(ConfigurationProfile configurationProfile) {
        this.sessionFactory
                .getCurrentSession().delete(configurationProfile);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getAll() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ConfigurationProfile");
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getAllByCloudPlatformId(long cloudPlatformId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from ConfigurationProfile c where c.cloudPlatform.cloudPlatformId = :cloudPlatformId")
                .setParameter("cloudPlatformId",cloudPlatformId);
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public SearchResultDTO search(SearchRequestDTO searchRequestDTO) {
        Long count = (Long) this.sessionFactory
                .getCurrentSession().createQuery("select count(*) from ConfigurationProfile").uniqueResult();

        CriteriaBuilder builder = this.sessionFactory
                .getCurrentSession().getCriteriaBuilder();

        CriteriaQuery<ConfigurationProfile> criteriaQuery = builder.createQuery(ConfigurationProfile.class);
        Root<ConfigurationProfile> root = criteriaQuery.from(ConfigurationProfile.class);
        criteriaQuery = criteriaQuery.where(
                builder.or(
                        builder.like(builder.lower(root.get("name")),
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
