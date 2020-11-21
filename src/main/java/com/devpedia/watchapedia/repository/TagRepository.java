package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.domain.Tag;
import com.devpedia.watchapedia.domain.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class TagRepository {

    private final EntityManager em;

    public void save(Tag tag) {
        em.persist(tag);
    }

    public void delete(Tag tag) {
        em.remove(tag);
    }

    public Tag findById(Long id) {
        return em.find(Tag.class, id);
    }

    public List<Tag> findListIn(Set<Long> set) {
        return em.createQuery(
                "select t " +
                        "from Tag t " +
                        "where t.id in :ids", Tag.class)
                .setParameter("ids", set)
                .getResultList();
    }

    public List<Tag> searchWithPaging(String search, int page, int size) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Tag> query = builder.createQuery(Tag.class);
        Root<Tag> from = query.from(Tag.class);
        CriteriaQuery<Tag> select = query.select(from);

        if (!StringUtils.isBlank(search))
            select = select.where(builder.like(from.get("description"), "%" + search + "%"));

        TypedQuery<Tag> tq = em.createQuery(select);

        tq.setFirstResult((page - 1) * size);
        tq.setMaxResults(size);

        return tq.getResultList();
    }
}
