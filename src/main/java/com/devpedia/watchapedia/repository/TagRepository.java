package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Content;
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

    /**
     * 태그 저장
     * @param tag 태그
     */
    public void save(Tag tag) {
        em.persist(tag);
    }

    /**
     * 태그 삭제
     * @param tag 태그
     */
    public void delete(Tag tag) {
        em.remove(tag);
    }

    /**
     * PK로 태그 조회
     * @param id tag_id
     * @return 조회된 태그 없으면 null
     */
    public Tag findById(Long id) {
        return em.find(Tag.class, id);
    }

    /**
     * 여러 아이디로 태그 조회
     * @param ids tag_id set
     * @return 조회된 태그 리스트
     */
    public List<Tag> findListIn(Set<Long> ids) {
        return em.createQuery(
                "select t " +
                        "from Tag t " +
                        "where t.id in :ids", Tag.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    /**
     * 설명(description)으로 태그를 검색해서 리스트로 반환한다.
     * @param query 검색어
     * @param page 페이지
     * @param size 반환 개수
     * @return 검색된 태그 리스트
     */
    public List<Tag> searchWithPaging(String query, int page, int size) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Tag> criteriaQuery = builder.createQuery(Tag.class);
        Root<Tag> from = criteriaQuery.from(Tag.class);
        CriteriaQuery<Tag> select = criteriaQuery.select(from);

        if (!StringUtils.isBlank(query))
            select = select.where(builder.like(from.get("description"), "%" + query + "%"));

        TypedQuery<Tag> tq = em.createQuery(select);

        tq.setFirstResult((page - 1) * size);
        tq.setMaxResults(size);

        return tq.getResultList();
    }

    /**
     * 전체 태그 중 특정 개수의 태그를 랜덤으로 조회한다.
     * @param size 조회 개수
     * @return 랜덤 태그 리스트
     */
    public List<Tag> getRandom(int size) {
        return em.createNativeQuery(
                "select * " +
                        "from tag t " +
                        "order by rand()", Tag.class)
                .setMaxResults(size)
                .getResultList();
    }
}
