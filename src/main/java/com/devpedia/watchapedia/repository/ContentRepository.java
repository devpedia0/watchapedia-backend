package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ContentRepository {

    private final EntityManager em;

    public void save(Content content) {
        em.persist(content);
    }

    public <T extends Content> T findById(Class<T> tClass, Long id) {
        return em.find(tClass, id);
    }

    public <T extends Content> List<T> findListIn(Class<T> tClass, Set<Long> ids) {
        return em.createQuery(
                "select c " +
                        "from "+ tClass.getSimpleName() + " c " +
                        "where c.id in :ids", tClass)
                .setParameter("ids", ids)
                .getResultList();
    }

    public <T extends Content> List<T> searchWithPaging(Class<T> tClass, String query, int page, int size) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = builder.createQuery(tClass);
        Root<T> from = criteriaQuery.from(tClass);
        CriteriaQuery<T> select = criteriaQuery.select(from);

        if (!StringUtils.isBlank(query))
            select = select.where(builder.like(from.get("mainTitle"), "%" + query + "%"));

        TypedQuery<T> tq = em.createQuery(select);

        tq.setFirstResult((page - 1) * size);
        tq.setMaxResults(size);

        return tq.getResultList();
    }
}
