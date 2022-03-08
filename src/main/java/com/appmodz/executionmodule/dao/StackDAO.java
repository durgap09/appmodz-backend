package com.appmodz.executionmodule.dao;

import com.appmodz.executionmodule.dto.SearchRequestDTO;
import com.appmodz.executionmodule.dto.SearchResultDTO;
import com.appmodz.executionmodule.model.Role;
import com.appmodz.executionmodule.model.Stack;
import com.appmodz.executionmodule.model.User;
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
import java.util.Map;

@Repository
public class StackDAO {

    private final
    SessionFactory sessionFactory;

    @Autowired
    public StackDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void save(Stack stack) {
        this.sessionFactory.getCurrentSession().saveOrUpdate(stack);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void setStackGroupNull(long stackGroupId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("UPDATE Stack s SET s.stackGroup = null WHERE s.stackGroup.stackGroupId =:stackGroupId")
                .setParameter("stackGroupId",stackGroupId);
        query.executeUpdate();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Stack get(long stackId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from Stack where stackId = :stackId")
                .setParameter("stackId",stackId);
        query.setMaxResults(1);
        return (Stack) query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getByStackGroupId(long stackGroupId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from Stack s WHERE s.stackGroup.stackGroupId =:stackGroupId")
                .setParameter("stackGroupId",stackGroupId);
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Stack get() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from Stack");
        query.setMaxResults(1);
        return (Stack) query.uniqueResult();
    }


    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void delete(Stack stack) {
        this.sessionFactory
                .getCurrentSession().delete(stack);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Long getCount() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("select count(*) from Stack");
        return (Long)query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getDeployedCount() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("select s.stackDeployedComponentsWithCount from Stack s");
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Long getCountDeployed() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("select count(*) from Stack s where s.stackIsDeployed=true");
        return (Long)query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getAll() {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from Stack");
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getByWorkspaceId(long workspaceId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("from Stack s where s.workspace.workspaceId = :workspaceId")
                .setParameter("workspaceId", workspaceId);
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Long getCountByOrganizationId(long organizationId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("select count(*) from Stack s where s.workspace.organization.organizationId=:organizationId")
                .setParameter("organizationId",organizationId);
        return (Long)query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Long getCountDeployedByOrganizationId(long organizationId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("select count(*) from Stack s where s.stackIsDeployed=true and s.workspace.organization.organizationId=:organizationId")
                .setParameter("organizationId",organizationId);
        return (Long)query.uniqueResult();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List getDeployedCountByOrganizationId(long organizationId) {
        Query query = this.sessionFactory
                .getCurrentSession()
                .createQuery("select s.stackDeployedComponentsWithCount from Stack s where s.stackIsDeployed=true and s.workspace.organization.organizationId=:organizationId")
                .setParameter("organizationId",organizationId);;
        return query.list();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public SearchResultDTO search(SearchRequestDTO searchRequestDTO) {
        Long count = (Long) this.sessionFactory
                .getCurrentSession().createQuery("select count(*) from Stack").uniqueResult();

        CriteriaBuilder builder = this.sessionFactory
                .getCurrentSession().getCriteriaBuilder();

        CriteriaQuery<Stack> criteriaQuery = builder.createQuery(Stack.class);
        Root<Stack> root = criteriaQuery.from(Stack.class);
        criteriaQuery.where(
                builder.or(
                                builder.like(builder.lower(root.get("terraformBackend").get("name")),
                                        "%"+ searchRequestDTO.getSearch().toLowerCase()+"%"),
                                builder.like(builder.lower(root.get("workspace").get("workspaceName")),
                                        "%"+ searchRequestDTO.getSearch().toLowerCase()+"%"),
                                builder.like(builder.lower(root.get("owner").get("userFirstName")),
                                        "%"+ searchRequestDTO.getSearch().toLowerCase()+"%")),
                                builder.like(builder.lower(root.get("owner").get("userLastName")),
                                        "%"+ searchRequestDTO.getSearch().toLowerCase()+"%"));
        List<Order> orderList = new ArrayList();
        if(searchRequestDTO.getSort().getAttribute().equals("name")) {
            if (searchRequestDTO.getSort().getSort().equals("desc")) {
                orderList.add(builder.desc(root.get("terraformBackend").get("name")));
            } else {
                orderList.add(builder.asc(root.get("terraformBackend").get("name")));
            }
        } else {
            if(searchRequestDTO.getSort().getAttribute().equals("id"))
                searchRequestDTO.getSort().setAttribute("stackId");
            if (searchRequestDTO.getSort().getSort().equals("desc")) {
                orderList.add(builder.desc(root.get(searchRequestDTO.getSort().getAttribute())));
            } else {
                orderList.add(builder.asc(root.get(searchRequestDTO.getSort().getAttribute())));
            }
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
