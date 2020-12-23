package com.devpedia.watchapedia.repository.user;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.dto.enums.InterestContentOrder;
import com.devpedia.watchapedia.dto.enums.RatingContentOrder;
import com.devpedia.watchapedia.util.UrlUtil;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.NullExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.devpedia.watchapedia.domain.QComment.*;
import static com.devpedia.watchapedia.domain.QContent.content;
import static com.devpedia.watchapedia.domain.QContentParticipant.contentParticipant;
import static com.devpedia.watchapedia.domain.QContentTag.*;
import static com.devpedia.watchapedia.domain.QImage.*;
import static com.devpedia.watchapedia.domain.QInterest.*;
import static com.devpedia.watchapedia.domain.QMovie.*;
import static com.devpedia.watchapedia.domain.QParticipant.participant;
import static com.devpedia.watchapedia.domain.QScore.*;
import static com.devpedia.watchapedia.domain.QTag.*;
import static com.devpedia.watchapedia.domain.QUser.*;
import static com.querydsl.core.types.dsl.Expressions.*;
import static com.querydsl.jpa.JPAExpressions.*;

@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public UserDto.UserActionCounts getUserActionCounts(Long id) {
        List<Tuple> result = query
                .select(
                        content.dtype,
                        score1.user.id.count(),
                        interestCountCase(InterestState.WISH).sum(),
                        interestCountCase(InterestState.WATCHING).sum(),
                        interestCountCase(InterestState.NOT_INTEREST).sum(),
                        comment.user.id.count()
                )
                .from(content)
                .leftJoin(score1).on(
                        content.id.eq(score1.id.contentId),
                        score1.id.userId.eq(id))
                .leftJoin(interest).on(
                        content.id.eq(interest.id.contentId),
                        interest.id.userId.eq(id))
                .leftJoin(comment).on(
                        content.id.eq(comment.id.contentId),
                        comment.id.userId.eq(id))
                .groupBy(content.dtype)
                .fetch();

        UserDto.UserActionCounts contents = UserDto.UserActionCounts.builder()
                .movie(UserDto.ActionCounts.zero())
                .book(UserDto.ActionCounts.zero())
                .tvShow(UserDto.ActionCounts.zero())
                .build();

        for (Tuple tuple : result) {
            Long ratingCount = tuple.get(1, Long.class);
            Integer wishCount = tuple.get(2, Integer.class);
            Integer watchingCount = tuple.get(3, Integer.class);
            Integer notInterestCount = tuple.get(4, Integer.class);
            Long commentCount = tuple.get(5, Long.class);

            UserDto.ActionCounts actionCounts = UserDto.ActionCounts.builder()
                    .ratingCount(ratingCount != null ? ratingCount.intValue() : 0)
                    .wishCount(wishCount != null ? wishCount : 0)
                    .watchingCount(watchingCount != null ? watchingCount : 0)
                    .notInterestCount(notInterestCount != null ? notInterestCount : 0)
                    .commentCount(commentCount != null ? commentCount.intValue() : 0)
                    .build();

            String dtype = tuple.get(0, String.class);

            if (ContentTypeParameter.MOVIES.getDtype().equals(dtype)) {
                contents.setMovie(actionCounts);
            } else if (ContentTypeParameter.BOOKS.getDtype().equals(dtype)) {
                contents.setBook(actionCounts);
            } else if (ContentTypeParameter.TV_SHOWS.getDtype().equals(dtype)){
                contents.setTvShow(actionCounts);
            }
        }

        return contents;
    }

    private NumberExpression<Integer> interestCountCase(InterestState state) {
        return interest.state.when(state).then(1).otherwise(0);
    }

    @Override
    public List<Score> findUserScores(Long userId, ContentTypeParameter type, Double score, RatingContentOrder order, Pageable pageable) {
        QScore score2 = new QScore("score2");
        NumberPath<Double> avg = Expressions.numberPath(Double.class, "average");

        List<Tuple> result = query
                .select(
                        score1,
                        as(select(score2.score.avg())
                                .from(score2)
                                .where(score2.content.id.eq(content.id)), avg)
                )
                .from(score1)
                .join(content).on(content.id.eq(score1.id.contentId))
                .where(
                        content.dtype.eq(type.getDtype()),
                        score1.id.userId.eq(userId),
                        scoreEq(score)
                )
                .orderBy(getOrder(order, avg))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return result.stream()
                .map(tuple -> tuple.get(score1))
                .collect(Collectors.toList());
    }

    private BooleanExpression scoreEq(Double score) {
        return score != null ? score1.score.eq(score) : null;
    }

    private OrderSpecifier<?> getOrder(RatingContentOrder order, NumberPath<Double> avg) {
        OrderSpecifier<?> os = new OrderSpecifier<>(Order.ASC, content.mainTitle);

        if (order == RatingContentOrder.AVG_SCORE)
            os = new OrderSpecifier<>(Order.DESC, avg);
        else if (order == RatingContentOrder.TITLE)
            os = new OrderSpecifier<>(Order.ASC, content.mainTitle);
        else if (order == RatingContentOrder.NEW)
            os = new OrderSpecifier<>(Order.DESC, content.productionDate);

        return os;
    }

    @Override
    public List<Score> findUserGroupedScore(Long userId, ContentTypeParameter type, int size) {
        return em.createNativeQuery(
                "select t.user_id, t.content_id, t.score " +
                        "from (select s.content_id, " +
                        "             user_id, " +
                        "             score, " +
                        "             row_number() over (partition by s.score order by c.main_title) as rowcount " +
                        "      from score s " +
                        "      join content c on c.content_id = s.content_id " +
                        "      where c.dtype = :type " +
                        "        and s.user_id = :userId) t " +
                        "where t.rowcount <= :size", Score.class)
                .setParameter("type", type.getDtype())
                .setParameter("userId", userId)
                .setParameter("size", size)
                .getResultList();
    }

    @Override
    public Map<String, Integer> getGroupedScoreCount(Long userId, ContentTypeParameter type) {
        List<Tuple> result = query
                .select(
                        score1.score.stringValue(),
                        score1.id.contentId.count()
                )
                .from(score1)
                .join(score1.content, content)
                .where(
                        content.dtype.eq(type.getDtype()),
                        score1.id.userId.eq(userId)
                )
                .groupBy(score1.score)
                .fetch();

        return result.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(score1.score.stringValue()),
                        tuple -> tuple.get(score1.id.contentId.count()).intValue()));
    }


    @Override
    public List<Interest> findUserInterestContent(Long userId, ContentTypeParameter type, InterestState state, InterestContentOrder order, Pageable pageable) {
        NumberPath<Double> avg = Expressions.numberPath(Double.class, "average");

        List<Tuple> result = query
                .select(
                        interest,
                        as(select(score1.score.avg())
                                .from(score1)
                                .where(score1.content.id.eq(content.id)), avg)
                )
                .from(interest)
                .join(content).on(content.id.eq(interest.id.contentId))
                .where(
                        content.dtype.eq(type.getDtype()),
                        interest.id.userId.eq(userId),
                        interest.state.eq(state)
                )
                .orderBy(getOrder(order, avg))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return result.stream()
                .map(tuple -> tuple.get(interest))
                .collect(Collectors.toList());
    }

    private OrderSpecifier<?> getOrder(InterestContentOrder order, NumberPath<Double> avg) {
        OrderSpecifier<?> os = new OrderSpecifier<>(Order.ASC, content.mainTitle);

        if (order == InterestContentOrder.AVG_SCORE)
            os = new OrderSpecifier<>(Order.DESC, avg);
        else if (order == InterestContentOrder.TITLE)
            os = new OrderSpecifier<>(Order.ASC, content.mainTitle);
        else if (order == InterestContentOrder.NEW)
            os = new OrderSpecifier<>(Order.DESC, content.productionDate);
        else if (order == InterestContentOrder.OLD)
            os = new OrderSpecifier<>(Order.ASC, content.productionDate);

        return os;
    }

    @Override
    public UserDto.UserRatingAnalysis getRatingAnalysis(Long id) {
        String sql =
                "select t.*, " +
                        "       (select case greatest(t.`0.0`, t.`0.5`, t.`1.0`, t.`1.5`, " +
                        "                             t.`2.0`, t.`2.5`, t.`3.0`, t.`3.5`, " +
                        "                             t.`4.0`, t.`4.5`, t.`5.0`) " +
                        "                when t.`0.0` then 0.0 " +
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
                        "             count(case when s.score = 0.0 then 1 end) as \"0.0\"," +
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

        LinkedHashMap<String, Integer> distribution = new LinkedHashMap<>();
        for (double d = 0.5; d <= 5.0; d += 0.5) {
            int objIndex = (int) ((d - 0.5) * 2 + 6);
            distribution.put(String.valueOf(d), ((BigInteger) result[objIndex]).intValue());
        }

        return UserDto.UserRatingAnalysis.builder()
                .totalCount(((BigInteger) result[0]).intValue())
                .movieCount(((BigInteger) result[1]).intValue())
                .bookCount(((BigInteger) result[2]).intValue())
                .tvShowCount(((BigInteger) result[3]).intValue())
                .average(result[4] != null ? (Double) result[4] : 0.0)
                .distribution(distribution)
                .mostRating(((BigDecimal) result[16]).doubleValue())
                .build();
    }

    @Override
    public List<UserDto.FavoritePerson> getFavoritePerson(Long id, ContentTypeParameter type, String job, int size) {
        NumberPath<Double> score = Expressions.numberPath(Double.class, "score");
        NumberPath<Integer> count = Expressions.numberPath(Integer.class, "cnt");

        List<Tuple> result = query
                .select(
                        participant.id,
                        image.path,
                        participant.name,
                        content.mainTitle.min(),
                        as(score1.score.avg(), score),
                        as(contentParticipant.participant.id.count().intValue(), count)
                )
                .from(contentParticipant)
                .join(contentParticipant.participant, participant)
                .join(contentParticipant.content, content)
                .join(participant.profileImage, image)
                .join(score1).on(
                        content.id.eq(score1.id.contentId),
                        score1.id.userId.eq(id))
                .where(
                        content.dtype.eq(type.getDtype()),
                        participant.job.eq(job)
                )
                .groupBy(contentParticipant.participant.id)
                .having(
                        contentParticipant.participant.id.count().gt(1),
                        score1.score.avg().goe(3.0))
                .orderBy(score.desc(), contentParticipant.participant.id.asc())
                .limit(size)
                .fetch();

        return result.stream()
                .map(tuple -> UserDto.FavoritePerson.builder()
                        .id(tuple.get(participant.id))
                        .profileImagePath(UrlUtil.getCloudFrontUrl(tuple.get(image.path)))
                        .name(tuple.get(participant.name))
                        .movieName((tuple.get(content.mainTitle)))
                        .score(tuple.get(score))
                        .count(tuple.get(count))
                        .build())
                .collect(Collectors.toList());

    }

    @Override
    public List<UserDto.FavoriteCommon> getFavoriteTag(Long id, ContentTypeParameter type, int size) {
        NumberPath<Double> score = Expressions.numberPath(Double.class, "score");
        NumberPath<Integer> count = Expressions.numberPath(Integer.class, "cnt");

        return query
                .select(
                        Projections.constructor(
                                UserDto.FavoriteCommon.class,
                                tag.description,
                                as(score1.score.avg(), score),
                                as(contentTag.id.tagId.count().intValue(), count)
                        )
                )
                .from(contentTag)
                .join(contentTag.tag, tag)
                .join(contentTag.content, content).on(content.dtype.eq(type.getDtype()))
                .join(score1).on(
                        content.id.eq(score1.id.contentId),
                        score1.id.userId.eq(id))
                .groupBy(contentTag.id.tagId)
                .having(
                        contentTag.id.tagId.count().gt(1),
                        score1.score.avg().goe(3.0))
                .orderBy(score.desc(), count.asc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<UserDto.FavoriteCommon> getFavoriteCountry(Long id, int size) {
        NumberPath<Double> score = Expressions.numberPath(Double.class, "score");
        NumberPath<Integer> count = Expressions.numberPath(Integer.class, "cnt");

        return query
                .select(
                        Projections.constructor(
                                UserDto.FavoriteCommon.class,
                                movie.countryCode,
                                as(score1.score.avg(), score),
                                as(movie.countryCode.count().intValue(), count)
                        )
                )
                .from(score1)
                .join(movie).on(score1.id.contentId.eq(movie.id))
                .where(score1.id.userId.eq(id))
                .groupBy(movie.countryCode)
                .having(
                        movie.countryCode.count().gt(1),
                        score1.score.avg().goe(3.0))
                .orderBy(score.desc(), count.asc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<UserDto.FavoriteCommon> getFavoriteCategory(Long id, ContentTypeParameter type, int size) {
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
                .setParameter("type", type.getDtype())
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

    @Override
    public int getTotalRunningTime(Long id) {
        Integer result = query
                .select(movie.runningTimeInMinutes.sum())
                .from(score1)
                .join(movie).on(score1.id.contentId.eq(movie.id))
                .where(score1.id.userId.eq(id))
                .fetchFirst();
        return result != null ? result : 0;
    }

    @Override
    public Map<Long, UserDto.ActionCounts> getActionCounts(Set<Long> ids) {
        List<Tuple> result = query
                .select(
                        user.id,
                        select(score1.id.userId.count().intValue()).from(score1).where(score1.id.userId.eq(user.id)),
                        select(comment.id.userId.count().intValue()).from(comment).where(comment.id.userId.eq(user.id)),
                        interestCountCase(InterestState.WISH).sum().intValue(),
                        interestCountCase(InterestState.WATCHING).sum().intValue(),
                        interestCountCase(InterestState.NOT_INTEREST).sum().intValue()
                )
                .from(user)
                .leftJoin(interest).on(interest.id.userId.eq(user.id))
                .where(user.id.in(ids))
                .groupBy(user.id)
                .fetch();

        return result.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(0, Long.class),
                        tuple -> UserDto.ActionCounts.builder()
                                .ratingCount(tuple.get(1, Integer.class))
                                .commentCount(tuple.get(2, Integer.class))
                                .wishCount(tuple.get(3, Integer.class))
                                .watchingCount(tuple.get(4, Integer.class))
                                .notInterestCount(tuple.get(5, Integer.class))
                                .build()));
    }

}
