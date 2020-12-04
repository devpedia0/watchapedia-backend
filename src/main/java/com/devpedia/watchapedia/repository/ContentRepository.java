package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.dto.ContentDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ContentRepository {

    private final EntityManager em;

    /**
     * Content 를 부모로하는 컨텐츠 저장.
     * @param content 저장하고자 하는 컨텐츠(영화, 책, 티비)
     */
    public void save(Content content) {
        em.persist(content);
    }

    /**
     * 컨텐츠 PK로 조회.
     * @param tClass 컨텐츠 종류 Class
     * @param id PK
     * @return 해당 엔티티
     */
    public <T extends Content> T findById(Class<T> tClass, Long id) {
        return em.find(tClass, id);
    }

    /**
     * 여러 id의 엔티티를 한번에 조회.
     * @param tClass 컨텐츠 종류 Class
     * @param ids PK Set
     * @return 해당 엔티티 리스트
     */
    public <T extends Content> List<T> findListIn(Class<T> tClass, Set<Long> ids) {
        return em.createQuery(
                "select c " +
                        "from "+ tClass.getSimpleName() + " c " +
                        "where c.id in :ids", tClass)
                .setParameter("ids", ids)
                .getResultList();
    }

    /**
     * 평점 평균이 지정 점수 이상인 컨텐츠를 개수만큼 조회한다.
     * @param tClass 컨텐츠 종류 Class
     * @param score 조회할 평점 기준
     * @param size 반환 개수
     * @return 평점이 이상인 컨텐츠 리스트
     */
    public <T extends Content> List<T> getContentsScoreIsGreaterThan(Class<T> tClass, double score, int size) {
        return em.createQuery(
                "select c " +
                        "from " + tClass.getSimpleName() +" c " +
                        "join fetch c.posterImage " +
                        "where (select avg(s.score) " +
                        "       from Score s " +
                        "       where s.content = c) >= :score", tClass)
                .setParameter("score", score)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * 해당 id로 조회한 컨텐츠들의 평균 평점을 맵 형태로 반환한다.
     * @param ids PK set
     * @return key: id, value: 평균 평점
     */
    public Map<Long, Double> getContentScore(Set<Long> ids) {
        List<Object[]> scores = em.createNativeQuery(
                "select s.content_id, " +
                        "       avg(s.score) as score " +
                        "from score s " +
                        "where s.content_id in :ids " +
                        "group by content_id ")
                .setParameter("ids", ids)
                .getResultList();

        return scores.stream()
                .collect(Collectors.toMap(o -> ((BigInteger) o[0]).longValue(), objects -> (Double) objects[1]));
    }

    /**
     * 해당 인물이 어떠한 역할로든 참여한 컨텐츠를 개수만큼 조회한다.
     * @param tClass 컨텐츠 종류 Class
     * @param participant 컨텐츠에 참여한 인물
     * @param size 반환 개수
     * @return 인물이 참여한 컨텐츠 리스트
     */
    public <T extends Content> List<T> getContentsHasParticipant(Class<T> tClass, Participant participant, int size) {
        return em.createQuery(
                "select m " +
                        "from ContentParticipant cp " +
                        "join cp.content c " +
                        "join "+ tClass.getSimpleName() +" m on m.id = c.id " +
                        "where cp.participant.id = :id", tClass)
                .setParameter("id", participant.getId())
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * 해당 태그가 포함된 컨텐츠를 개수만큼 반환한다.
     * @param tClass 컨텐츠 종류 Class
     * @param tag 컨텐츠에 걸린 태그
     * @param size 반환 개수
     * @return 태그가 걸린 컨텐츠 리스트
     */
    public <T extends Content> List<T> getContentsTagged(Class<T> tClass, Tag tag, int size) {
        return em.createQuery(
                "select m " +
                        "from ContentTag ct " +
                        "join ct.content c " +
                        "join " + tClass.getSimpleName() +" m on m.id = c.id " +
                        "where ct.tag.id = :id", tClass)
                .setParameter("id", tag.getId())
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * 해당 컬렉션에 포함되는 컨텐츠를 개수만큼 반환한다.
     * @param tClass 컨텐츠 종류 Class
     * @param collection 컬렉션
     * @param size 반환 개수
     * @return 컬렉션에 담긴 컨텐츠 리스트
     */
    public <T extends Content> List<T> getContentsInCollection(Class<T> tClass, Collection collection, int size) {
        return em.createQuery(
                "select m " +
                        "from CollectionContent cc " +
                        "join cc.content c " +
                        "join " +  tClass.getSimpleName() + " m on m.id = c.id " +
                        "where cc.collection.id = :id", tClass)
                .setParameter("id", collection.getId())
                .setMaxResults(size)
                .getResultList();
    }
}
