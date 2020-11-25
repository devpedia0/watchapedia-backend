package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Participant;
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
public class ParticipantRepository {

    private final EntityManager em;

    public void save(Participant participant) {
        em.persist(participant);
    }

    public void delete(Participant participant) {
        em.remove(participant);
    }

    public Participant findById(Long id) {
        return em.find(Participant.class, id);
    }

    public List<Participant> findListIn(Set<Long> set) {
        return em.createQuery(
                "select p " +
                        "from Participant p " +
                        "where p.id in :ids", Participant.class)
                .setParameter("ids", set)
                .getResultList();
    }

    public List<Participant> searchWithPaging(String query, int page, int size) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Participant> criteriaQuery = builder.createQuery(Participant.class);
        Root<Participant> from = criteriaQuery.from(Participant.class);
        CriteriaQuery<Participant> select = criteriaQuery.select(from);

        if (!StringUtils.isBlank(query))
            select = select.where(builder.like(from.get("name"), "%" + query + "%"));

        TypedQuery<Participant> tq = em.createQuery(select);

        tq.setFirstResult((page - 1) * size);
        tq.setMaxResults(size);

        return tq.getResultList();
    }
}
