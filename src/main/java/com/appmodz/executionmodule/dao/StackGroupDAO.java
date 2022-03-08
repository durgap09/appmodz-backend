package com.appmodz.executionmodule.dao;

import com.appmodz.executionmodule.dto.SearchRequestDTO;
import com.appmodz.executionmodule.dto.SearchResultDTO;
import com.appmodz.executionmodule.model.Stack;
import com.appmodz.executionmodule.model.StackGroup;
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
public class StackGroupDAO {
    private final
    SessionFactory sessionFactory;

    @Autowired
    public StackGroupDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(StackGroup stackGroup) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(stackGroup);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public StackGroup get(long stackGroupId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from StackGroup where stackGroupId = :stackGroupId")
                .setParameter("stackGroupId",stackGroupId);
        query.setMaxResults(1);
        return (StackGroup) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getAll() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from StackGroup");
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public StackGroup getStacksOfStackGroup(long stackGroupId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("SELECT s FROM StackGroup s LEFT JOIN FETCH s.stacks WHERE s.stackGroupId=:stackGroupId")
                .setParameter("stackGroupId",stackGroupId);
        query.setMaxResults(1);
        return (StackGroup) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(StackGroup stackGroup) {
        this.sessionFactory
                .getCurrentSession().delete(stackGroup);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public SearchResultDTO search(SearchRequestDTO searchRequestDTO) {
        Long count = (Long) this.sessionFactory
                .getCurrentSession().createQuery("select count(*) from StackGroup").uniqueResult();

        CriteriaBuilder builder = this.sessionFactory
                .getCurrentSession().getCriteriaBuilder();

        CriteriaQuery<StackGroup> criteriaQuery = builder.createQuery(StackGroup.class);
        Root<StackGroup> root = criteriaQuery.from(StackGroup.class);
        criteriaQuery.where(
                builder.or(
                        builder.like(builder.lower(root.get("stackGroupName")),
                                "%"+ searchRequestDTO.getSearch().toLowerCase()+"%")));
        List<Order> orderList = new ArrayList();
            if(searchRequestDTO.getSort().getAttribute().equals("id"))
                searchRequestDTO.getSort().setAttribute("stackGroupId");
            if (searchRequestDTO.getSort().getSort().equals("desc")) {
                orderList.add(builder.desc(root.get(searchRequestDTO.getSort().getAttribute())));
            } else {
                orderList.add(builder.asc(root.get(searchRequestDTO.getSort().getAttribute())));
            }
        criteriaQuery.orderBy(orderList);
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
