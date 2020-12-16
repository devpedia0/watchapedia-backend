package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.dto.enums.InterestContentOrder;
import com.devpedia.watchapedia.dto.enums.RatingContentOrder;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
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
     * 컨텐츠별 유저가 매긴 평점과 보고싶어요, 보는중, 관심없어요, 코멘트 개수를 구한다.
     * @param id user_id
     * @return 컨텐츠 별 유저 활동(평점, 보고싶어요, 보는중, 관심없어요, 코멘트) 개수
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

    /**
     * 유저 평점 분석 정보를 반환한다. 구성은
     * - 총 평점 개수
     * - 컨텐츠 별 평점 개수 (영화, 책, 티비)
     * - 평점 평균
     * - 많이 준 평점
     * - 평점 별 분포
     * @param id 조회 대상 유저 ID
     * @return 유저 평점 분석 정보
     */
    public UserDto.UserRatingAnalysis getRatingAnalysis(Long id) {
        String sql =
                "select t.*, " +
                "       (select case greatest(t.`0.5`, t.`1.0`, t.`1.5`, t.`2.0`, " +
                "                             t.`2.5`, t.`3.0`, t.`3.5`, t.`4.0`, " +
                "                             t.`4.5`, t.`5.0`) " +
                "                when t.`0.5` then 0.5 " +
                "                when t.`1.0` then 1.0 " +
                "                when t.`1.5` then 1.5 " +
                "                when t.`2.0` then 2.0 " +
                "                when t.`2.5` then 2.5 " +
                "                when t.`3.0` then 3.0 " +
                "                when t.`3.5` then 3.5 " +
                "                when t.`4.0` then 4.0 " +
                "                when t.`4.5` then 4.5 " +
                "                when t.`5.0` then 5.0 " +
                "               end) as most " +
                "from (select count(*) as total, " +
                "             count(case when c.dtype = 'M' then 1 end) as movie, " +
                "             count(case when c.dtype = 'B' then 1 end) as book, " +
                "             count(case when c.dtype = 'S' then 1 end) as tv_show, " +
                "             avg(s.score) as average, " +
                "             count(case when s.score = 0.5 then 1 end) as \"0.5\", " +
                "             count(case when s.score = 1.0 then 1 end) as \"1.0\", " +
                "             count(case when s.score = 1.5 then 1 end) as \"1.5\", " +
                "             count(case when s.score = 2.0 then 1 end) as \"2.0\", " +
                "             count(case when s.score = 2.5 then 1 end) as \"2.5\", " +
                "             count(case when s.score = 3.0 then 1 end) as \"3.0\", " +
                "             count(case when s.score = 3.5 then 1 end) as \"3.5\", " +
                "             count(case when s.score = 4.0 then 1 end) as \"4.0\", " +
                "             count(case when s.score = 4.5 then 1 end) as \"4.5\", " +
                "             count(case when s.score = 5.0 then 1 end) as \"5.0\" " +
                "      from score s " +
                "      join content c on c.content_id = s.content_id " +
                "      where user_id = :id) as t";

        Object[] result = (Object[]) em.createNativeQuery(sql)
                .setParameter("id", id)
                .getSingleResult();

        LinkedHashMap<Double, Integer> distribution = new LinkedHashMap<>();
        for (double d = 0.5; d <= 5.0; d += 0.5) {
            int objIndex = (int) ((d - 0.5) * 2 + 5);
            distribution.put(d, ((BigInteger) result[objIndex]).intValue());
        }

        return UserDto.UserRatingAnalysis.builder()
                .totalCount(((BigInteger) result[0]).intValue())
                .movieCount(((BigInteger) result[1]).intValue())
                .bookCount(((BigInteger) result[2]).intValue())
                .tvShowCount(((BigInteger) result[3]).intValue())
                .average((Double) result[4])
                .distribution(distribution)
                .mostRating(((BigDecimal) result[15]).doubleValue())
                .build();
    }

    /**
     * 유저의 직업 별 선호하는 인물 리스트를 구한다.
     * @param id 조회 대상 유저 ID
     * @param type 컨텐츠 타입 (M, B, S)
     * @param job 인물의 직업
     * @param size 사이즈
     * @return 선호하는 인물 리스트
     */
    public List<UserDto.FavoritePerson> getFavoritePerson(Long id, String type, String job, int size) {
        List<Object[]> result = em.createQuery(
                "select p.id, " +
                        "       i.path, " +
                        "       p.name, " +
                        "       c.mainTitle, " +
                        "       avg(s.score) as score, " +
                        "       count(cp.participant) " +
                        "from ContentParticipant cp " +
                        "join cp.participant p " +
                        "join cp.content c " +
                        "join p.profileImage i " +
                        "join Score s on c.id = s.content.id " +
                        "and s.user.id = :id " +
                        "where c.dtype = :type " +
                        "  and p.job = :job " +
                        "group by cp.participant " +
                        "having count(cp.participant) > 1" +
                        "  and avg(s.score) >= 3.0 " +
                        "order by score desc, cp.participant.id", Object[].class)
                .setParameter("id", id)
                .setParameter("type", type)
                .setParameter("job", job)
                .setMaxResults(size)
                .getResultList();

        return result.stream()
                .map(obj -> UserDto.FavoritePerson.builder()
                        .id((Long) obj[0])
                        .profileImagePath(UrlUtil.getCloudFrontUrl((String) obj[1]))
                        .name((String) obj[2])
                        .movieName((String) obj[3])
                        .score((Double) obj[4])
                        .count(((Long) obj[5]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 유저의 선호하는 태그 리스트를 구한다.
     * @param id 조회 대상 유저 ID
     * @param type 컨텐츠 타입 (M, B, S)
     * @param size 사이즈
     * @return 선호하는 태그 리스트
     */
    public List<UserDto.FavoriteCommon> getFavoriteTag(Long id, String type, int size) {
        List<Object[]> result = em.createQuery(
                "select t.description, " +
                        "       avg(s.score) as score, " +
                        "       count(ct.tag) as cnt " +
                        "from ContentTag ct " +
                        "join ct.tag t " +
                        "join ct.content c on c.dtype = :type " +
                        "join Score s on c.id = s.content.id " +
                        "and s.user.id = :id " +
                        "group by ct.tag " +
                        "having count(ct.tag) > 1" +
                        "  and avg(s.score) >= 3.0 " +
                        "order by score desc, cnt ", Object[].class)
                .setParameter("id", id)
                .setParameter("type", type)
                .setMaxResults(size)
                .getResultList();

        return result.stream()
                .map(obj -> UserDto.FavoriteCommon.builder()
                        .description((String) obj[0])
                        .score((Double) obj[1])
                        .count(((Long) obj[2]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 유저의 선호하는 국가 리스트를 구한다.
     * 현재 영화에 대해서만 가능하다.
     * @param id 조회 대상 유저 ID
     * @param size 사이즈
     * @return 선호하는 국가 리스트
     */
    public List<UserDto.FavoriteCommon> getFavoriteCountry(Long id, int size) {
        List<Object[]> result = em.createQuery(
                "select m.countryCode, " +
                        "       avg(s.score) as score, " +
                        "       count(m.countryCode) as cnt " +
                        "from Score s " +
                        "join s.content c " +
                        "join Movie m on m.id = c.id " +
                        "where s.user.id = :id " +
                        "group by m.countryCode " +
                        "having count(m.countryCode) > 1" +
                        "  and avg(s.score) >= 3.0 " +
                        "order by score desc, cnt ", Object[].class)
                .setParameter("id", id)
                .setMaxResults(size)
                .getResultList();

        return result.stream()
                .map(obj -> UserDto.FavoriteCommon.builder()
                        .description((String) obj[0])
                        .score((Double) obj[1])
                        .count(((Long) obj[2]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 유저의 선호하는 카테고리 리스트를 구한다.
     * 현재 카테고리는 테이블이 분리되어 있지 않고
     * 한 칼럼에 구분자(/)로 나눠서 들어가 있다.
     * 따라서 한 컨텐츠에 있는 최대 카테고리 개수(현재는 6)만큼
     * 구분자(/)로 잘라서 row 로 얻은 다음 처리하고 있음.
     * @param id 조회 대상 유저 ID
     * @param type 컨텐츠 타입 (M, B, S)
     * @param size 사이즈
     * @return 선호하는 카테고리 리스트
     */
    public List<UserDto.FavoriteCommon> getFavoriteCategory(Long id, String type, int size) {
        List<Object[]> result = em.createNativeQuery(
                "select substring_index(substring_index(c.category, '/', numbers.n), '/', -1) as categories, " +
                        "       avg(s.score) as score," +
                        "       count(c.content_id) as count " +
                        "from (select 1 n union all select 2 union all " +
                        "      select 3 union all select 4 union all " +
                        "      select 5 union all select 6) numbers " +
                        "join content c " +
                        "    on c.category != '' " +
                        "    and c.dtype = :type " +
                        "    and char_length(c.category) - char_length(replace(c.category, '/', '')) >= numbers.n - 1 " +
                        "join score s on c.content_id = s.content_id " +
                        "    and s.user_id = :id " +
                        "group by categories " +
                        "having count(c.content_id) > 1 " +
                        "   and avg(s.score) >= 3.0 " +
                        "order by score desc, count")
                .setParameter("id", id)
                .setParameter("type", type)
                .setMaxResults(size)
                .getResultList();

        return result.stream()
                .map(obj -> UserDto.FavoriteCommon.builder()
                        .description((String) obj[0])
                        .score((Double) obj[1])
                        .count(((BigInteger) obj[2]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 유저의 총 영화 감상 시간을 구한다.
     * 평점을 매긴 작품을 감상한 것으로 본다.
     * @param userId 조회 대상 유저 ID
     * @return 총 영화 감상 시간
     */
    public int getTotalRunningTime(Long userId) {
        return em.createQuery(
                "select sum(m.runningTimeInMinutes) " +
                        "from Score s " +
                        "join Movie m on s.content = m " +
                        "where s.user.id = :id", Long.class)
                .setParameter("id", userId)
                .getSingleResult()
                .intValue();
    }
}
