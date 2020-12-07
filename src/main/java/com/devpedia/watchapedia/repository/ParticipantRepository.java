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

    /**
     * 인물 저장
     * @param participant 인물
     */
    public void save(Participant participant) {
        em.persist(participant);
    }

    /**
     * 인물 삭제
     * @param participant 인물
     */
    public void delete(Participant participant) {
        em.remove(participant);
    }

    /**
     * 인물 PK로 조회
     * @param id participant_id
     * @return 조회된 인물 없으면 null
     */
    public Participant findById(Long id) {
        return em.find(Participant.class, id);
    }

    /**
     * 여러개의 PK로 인물 조회
     * @param set participant_id set
     * @return 조회된 인물 리스트
     */
    public List<Participant> findListIn(Set<Long> set) {
        return em.createQuery(
                "select p " +
                        "from Participant p " +
                        "where p.id in :ids", Participant.class)
                .setParameter("ids", set)
                .getResultList();
    }

    /**
     * 컨텐츠 배치 삽입용 API
     * 인물 이름으로 조회, 동명이인 시 최상단 조회값을 리턴한다.
     * @param name 검색 이름
     * @return 조회된 인물 or null
     */
    public Participant findByName(String name) {
        List<Participant> result = em.createQuery(
                "select p " +
                        "from Participant p " +
                        "where p.name = :name", Participant.class)
                .setParameter("name", name)
                .getResultList();
        return result.size() == 0 ? null : result.get(0);
    }

    /**
     * 이름으로 인물을 검색해서 리스트로 반환한다.
     * @param query 검색 이름
     * @param page 페이지
     * @param size 반환 개수
     * @return 검색된 인물 리스트
     */
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

    /**
     * 해당하는 컨텐츠 타입 중 가장 많이 작품에 참여한
     * 해당 직업의 인물을 구한다. (ex. 영화에 가장 많아 참여한 감독) 
     * @param type 컨텐츠 타입 String(M, B, S)
     * @param job 직업
     * @return 가장 많이 참여한 인물
     */
    public Participant findMostParticipatedHasJob(String type, String job) {
        List<Participant> result = em.createQuery(
                "select p " +
                        "from ContentParticipant cp " +
                        "join cp.participant p " +
                        "join cp.content c " +
                        "where p.job like :job " +
                        "and c.dtype = :type " +
                        "group by cp.participant " +
                        "order by count(cp.participant) desc", Participant.class)
                .setParameter("job", "%" + job + "%")
                .setParameter("type", type)
                .setMaxResults(1)
                .getResultList();
        return result.size() == 0 ? null : result.get(0);
    }
}
