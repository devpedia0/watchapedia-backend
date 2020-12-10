package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.dto.enums.InterestContentOrder;
import com.devpedia.watchapedia.dto.enums.RatingContentOrder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    /**
     * 유저 저장
     * @param user 유저
     */
    public void save(User user) {
        em.persist(user);
    }

    /**
     * PK로 유저 조회
     * @param id user_id
     * @return 조회된 유저 없으면 null
     */
    public User findById(Long id) {
        return em.find(User.class, id);
    }

    /**
     * 이메일로 유저를 조회한다.
     * 이메일은 unique 이므로 2건이상 조회되지 않는다.
     * @param email 검색 이메일
     * @return 유저
     */
    public User findByEmail(String email) {
        List<User> result = em.createQuery(
                "select u " +
                        "from User u " +
                        "where u.email = :email", User.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * 컨텐츠별 유저가 매긴 평점와 보고싶어요, 보는중, 관심없어요, 코멘트 개수를 구한다.
     * @param id user_id
     * @return 컨텐츠 별 유저 활동 개수
     */
    public UserDto.UserActionCounts getUserActionCounts(Long id) {
        List<Object[]> resultList = em.createNativeQuery(
                "select a.dtype as type, " +
                        "    count(b.user_id) as ratingCount, " +
                        "    count(case when i.state = 1 then 1 end) as wishCount, " +
                        "    count(case when i.state = 2 then 1 end) as watchingCount, " +
                        "    count(case when i.state = 3 then 1 end) as notInterestCount, " +
                        "    count(c.user_id) as commentCount " +
                        "from content a " +
                        "left outer join score b on a.content_id = b.content_id " +
                        "    and b.user_id = :id " +
                        "left outer join interest i on a.content_id = i.content_id " +
                        "    and i.user_id = :id " +
                        "left outer join comment c on a.content_id = c.content_id " +
                        "    and c.user_id = :id " +
                        "group by a.dtype")
                .setParameter("id", id)
                .getResultList();

        UserDto.UserActionCounts contents = UserDto.UserActionCounts.builder().build();
        for (Object[] objects : resultList) {
            UserDto.ActionCounts actionCounts = UserDto.ActionCounts.builder()
                    .ratingCount(((BigInteger) objects[1]).intValue())
                    .wishCount(((BigInteger) objects[2]).intValue())
                    .watchingCount(((BigInteger) objects[3]).intValue())
                    .notInterestCount(((BigInteger) objects[4]).intValue())
                    .commentCount(((BigInteger) objects[5]).intValue())
                    .build();
            if ((objects[0]).equals("M")) {
                contents.setMovie(actionCounts);
            } else if ((objects[0]).equals("B")) {
                contents.setBook(actionCounts);
            } else {
                contents.setTvShow(actionCounts);
            }
        }

        return contents;
    }

    /**
     * 유저가 평가한 평점(Score)을 구한다.
     * @param userId 조회 대상 유저
     * @param type 컨텐츠 타입(M, B, S)
     * @param score 점수(0.5 ~ 5.0) null 이면 전체 조회
     * @param order 정렬 방식
     * @param page 페이지
     * @param size 사이즈
     * @return 유저 평점 리스트
     */
    public <T extends Content> List<Score> findUserScores(Long userId, String type, Double score, RatingContentOrder order,
                                                          Integer page, Integer size) {
        String sql =
                "select s.score, " +
                "       s.user_id, " +
                "       c.*, " +
                "       (select avg(score) " +
                "        from score s2 " +
                "        where s2.content_id = c.content_id) as avg " +
                "from score s " +
                "join content c on c.content_id = s.content_id " +
                "where c.dtype = :type " +
                "  and s.user_id = :userId ";
        if (score != null)
            sql += " and s.score = :score ";

        if (order == RatingContentOrder.AVG_SCORE)
            sql += "order by avg desc ";
        else if (order == RatingContentOrder.TITLE)
            sql += "order by c.main_title ";
        else if (order == RatingContentOrder.NEW)
            sql += "order by c.production_date desc ";

        Query query = em.createNativeQuery(sql, Score.class)
                .setParameter("userId", userId)
                .setParameter("type", type)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size);
        if (score != null)
            query.setParameter("score", score);

        return query.getResultList();
    }

    /**
     * 유저가 평가한 평점을 평점(0.5 ~ 5.0)별로 개수만큼 가져온다.
     * 정렬은 작품 main_title 가나다 순 고정
     * @param userId 조회 대상 유저
     * @param type 컨텐츠 타입(M, B, S)
     * @param size 각 평점별 조회 개수
     * @return 평점 리스트
     */
    public <T extends Content> List<Score> findUserGroupedScore(Long userId, String type, int size) {
        return em.createNativeQuery(
                "select t.user_id, t.content_id, t.score " +
                        "from (select s.content_id, " +
                        "             user_id, " +
                        "             score, " +
                        "             row_number() over (partition by s.score order by c.main_title) as row " +
                        "      from score s " +
                        "      join content c on c.content_id = s.content_id " +
                        "      where c.dtype = :type " +
                        "        and s.user_id = :userId) t " +
                        "where t.row <= :size", Score.class)
                .setParameter("type", type)
                .setParameter("userId", userId)
                .setParameter("size", size)
                .getResultList();
    }

    /**
     * 평점(0.5 ~ 5.0)별 평가한 작품 개수를 Map 형태로 가져온다.
     * key: 평점, value: 작품 개수
     * @param userId 조회 대상 유저
     * @param type 컨텐츠 타입(M, B, S)
     * @return 평점별 평가한 작품 개수
     */
    public Map<Double, Integer> getGroupedScoreCount(Long userId, String type) {
        List<Object[]> counts = em.createNativeQuery(
                "select s.score, count(*) " +
                        "    from score s " +
                        "    join content c on c.content_id = s.content_id " +
                        "    where c.dtype = :type " +
                        "    and s.user_id = :userId" +
                        "    group by s.score")
                .setParameter("type", type)
                .setParameter("userId", userId)
                .getResultList();

        return counts.stream()
                .collect(Collectors.toMap(o -> (Double) o[0], objects -> ((BigInteger) objects[1]).intValue()));
    }

    /**
     * 유저의 보고싶어요, 보는중, 관심없음에 해당하는 작품을 가져온다.
     * @param userId 조회 대상 유저
     * @param type 컨텐츠 타입(M, B, S)
     * @param state 관심 종류(보고싶어요, 보는중, 관심없음)
     * @param order 정렬 방식
     * @param page 페이지
     * @param size 사이즈
     * @return 관심 리스트
     */
    public List<Interest> findUserInterestContent(Long userId, String type, Integer state, InterestContentOrder order,
                                                  Integer page, Integer size) {
        String sql =
                "select i.content_id, user_id, state, " +
                "       c.*, " +
                "       (select avg(score) " +
                "        from score s " +
                "        where s.content_id = c.content_id) as avg " +
                "from interest i " +
                "join content c on c.content_id = i.content_id " +
                "where c.dtype = :type " +
                "  and i.user_id = :userId " +
                "  and i.state = :state ";

        if (order == InterestContentOrder.AVG_SCORE)
            sql += "order by avg desc ";
        else if (order == InterestContentOrder.TITLE)
            sql += "order by c.main_title ";
        else if (order == InterestContentOrder.NEW)
            sql += "order by c.production_date desc ";
        else if (order == InterestContentOrder.OLD)
            sql += "order by c.production_date ";

        Query query = em.createNativeQuery(sql, Interest.class)
                .setParameter("userId", userId)
                .setParameter("type", type)
                .setParameter("state", state)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size);

        return query.getResultList();
    }
}
