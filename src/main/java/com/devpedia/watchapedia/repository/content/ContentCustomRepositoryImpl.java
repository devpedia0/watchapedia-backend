package com.devpedia.watchapedia.repository.content;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.dto.DetailDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.devpedia.watchapedia.domain.QCollection.collection;
import static com.devpedia.watchapedia.domain.QCollectionContent.collectionContent;
import static com.devpedia.watchapedia.domain.QComment.*;
import static com.devpedia.watchapedia.domain.QCommentLike.*;
import static com.devpedia.watchapedia.domain.QContent.content;
import static com.devpedia.watchapedia.domain.QContentParticipant.*;
import static com.devpedia.watchapedia.domain.QContentTag.*;
import static com.devpedia.watchapedia.domain.QImage.*;
import static com.devpedia.watchapedia.domain.QInterest.*;
import static com.devpedia.watchapedia.domain.QReply.*;
import static com.devpedia.watchapedia.domain.QScore.*;
import static com.devpedia.watchapedia.domain.QUser.*;
import static com.querydsl.core.types.ExpressionUtils.*;
import static com.querydsl.core.types.Projections.*;
import static com.querydsl.jpa.JPAExpressions.*;

@RequiredArgsConstructor
public class ContentCustomRepositoryImpl implements ContentCustomRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public List<Content> getContentsScoreIsGreaterThan(ContentTypeParameter type, double score, int size) {
        return query
                .select(content)
                .from(content)
                .join(content.posterImage, image).fetchJoin()
                .where(select(score1.score.avg())
                        .from(score1)
                        .where(score1.content.id.eq(content.id))
                        .goe(score),
                        content.dtype.eq(type.getDtype())
                )
                .limit(size)
                .fetch();
    }

    @Override
    public Map<Long, Double> getContentScore(Set<Long> ids) {
        List<Tuple> list = query
                .select(score1.content.id, score1.score.avg())
                .from(score1)
                .where(score1.id.contentId.in(ids))
                .groupBy(score1.id.contentId)
                .fetch();
        return list.stream()
                .collect(Collectors.toMap(tuple -> tuple.get(0, Long.class), tuple -> tuple.get(1, Double.class)));
    }

    @Override
    public List<Content> getContentsHasParticipant(ContentTypeParameter type, Long participantId, int size) {
        return query
                .select(content)
                .from(contentParticipant)
                .join(contentParticipant.content, content).on(content.dtype.eq(type.getDtype()))
                .where(contentParticipant.participant.id.eq(participantId))
                .limit(size)
                .fetch();
    }

    @Override
    public List<Content> getContentsTagged(ContentTypeParameter type, Long tagId, int size) {
        return query
                .select(content)
                .from(contentTag)
                .join(contentTag.content, content).on(content.dtype.eq(type.getDtype()))
                .where(contentTag.tag.id.eq(tagId))
                .limit(size)
                .fetch();
    }

    @Override
    public List<Content> getContentsInCollection(Long collectionId, Pageable pageable) {
        return query
                .select(content)
                .from(collectionContent)
                .join(collectionContent.content, content)
                .where(collectionContent.collection.id.eq(collectionId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<String> getTrendingWords(int size) {
        NumberPath<Long> commentCount = Expressions.numberPath(Long.class, "comment_count");
        List<Tuple> result = query
                .select(
                        content.mainTitle,
                        as(select(comment.id.contentId.count())
                                .from(comment)
                                .where(comment.content.id.eq(content.id)), commentCount)
                )
                .from(content)
                .orderBy(commentCount.desc())
                .limit(size)
                .fetch();

        return result.stream()
                .map(tuple -> tuple.get(content.mainTitle))
                .collect(Collectors.toList());
    }


    @Override
    public <T extends Content> T initializeAndUnproxy(T entity) {
        if (entity == null) throw new NullPointerException("Entity passed for initialization is null");

        Hibernate.initialize(entity);

        if (entity instanceof HibernateProxy)
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();

        return entity;
    }


    @Override
    public DetailDto.UserContext getUserContext(Long contentId, Long userId) {
        return query
                .select(
                        constructor(
                                DetailDto.UserContext.class,
                                score1.id.userId,
                                interest.state,
                                score1.score,
                                comment.description
                        )
                )
                .from(content)
                .leftJoin(score1).on(
                        score1.id.contentId.eq(content.id),
                        score1.user.id.eq(userId)
                )
                .leftJoin(interest).on(
                        interest.id.contentId.eq(content.id),
                        interest.user.id.eq(userId)
                )
                .leftJoin(comment).on(
                        comment.id.contentId.eq(content.id),
                        comment.user.id.eq(userId)
                )
                .where(content.id.eq(contentId))
                .fetchOne();
    }

    @Override
    public DetailDto.ScoreAnalysis getScoreAnalysis(Long id) {
        Tuple tuple = query
                .select(
                        score1.id.contentId.count(),
                        score1.score.avg(),
                        scoreCountCase(0.5).sum().intValue(),
                        scoreCountCase(1.0).sum().intValue(),
                        scoreCountCase(1.5).sum().intValue(),
                        scoreCountCase(2.0).sum().intValue(),
                        scoreCountCase(2.5).sum().intValue(),
                        scoreCountCase(3.0).sum().intValue(),
                        scoreCountCase(3.5).sum().intValue(),
                        scoreCountCase(4.0).sum().intValue(),
                        scoreCountCase(4.5).sum().intValue(),
                        scoreCountCase(5.0).sum().intValue()
                )
                .from(score1)
                .where(score1.id.contentId.eq(id))
                .fetchOne();
        if (tuple == null) return null;
        Long count = tuple.get(0, Long.class);
        Double avg = tuple.get(1, Double.class);
        LinkedHashMap<String, Integer> distribution = new LinkedHashMap<>();
        for (double d = 0.5; d <= 5.0; d += 0.5) {
            int objIndex = (int) ((d - 0.5) * 2 + 2);
            distribution.put(String.valueOf(d), tuple.get(objIndex, Integer.class));
        }

        return DetailDto.ScoreAnalysis.builder()
                .totalCount(count != null ? count.intValue() : 0)
                .average(avg != null ? avg : 0.0)
                .distribution(distribution)
                .build();
    }

    private NumberExpression<Integer> scoreCountCase(Double score) {
        return score1.score.when(score).then(1).otherwise(0);
    }

    @Override
    public List<DetailDto.CommentDetail> getComments(Long contentId, Long userId, Pageable pageable) {
        NumberPath<Long> replyCount = Expressions.numberPath(Long.class, "replyCount");
        NumberPath<Long> likeCount = Expressions.numberPath(Long.class, "likeCount");
        NumberPath<Long> isLiked = Expressions.numberPath(Long.class, "isLiked");

        List<Tuple> result = query
                .select(
                        user.id,
                        user.name,
                        comment.description,
                        comment.containsSpoiler,
                        as(select(reply.id.count())
                                .from(reply)
                                .where(reply.comment.id.contentId.eq(comment.id.contentId),
                                        reply.comment.id.userId.eq(comment.id.userId)), replyCount),
                        as(select(commentLike.id.likeUserId.count())
                                .from(commentLike)
                                .where(commentLike.comment.id.contentId.eq(comment.id.contentId),
                                        commentLike.comment.id.userId.eq(comment.id.userId)), likeCount),
                        interest.state,
                        score1.score,
                        as(select(commentLike.id.likeUserId)
                                .from(commentLike)
                                .where(commentLike.comment.id.contentId.eq(comment.id.contentId),
                                        commentLike.comment.id.userId.eq(comment.id.userId),
                                        commentLike.id.likeUserId.eq(userId)), isLiked)
                )
                .from(comment)
                .join(comment.content, content)
                .join(comment.user, user)
                .leftJoin(interest).on(
                        interest.id.userId.eq(user.id),
                        interest.id.contentId.eq(content.id)
                )
                .leftJoin(score1).on(
                        score1.id.userId.eq(user.id),
                        score1.id.contentId.eq(content.id)
                )
                .where(comment.id.contentId.eq(contentId))
                .orderBy(likeCount.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return result.stream()
                .map(tuple -> DetailDto.CommentDetail.builder()
                        .userId(tuple.get(user.id))
                        .userName(tuple.get(user.name))
                        .description(tuple.get(comment.description))
                        .isSpoiler(tuple.get(comment.containsSpoiler))
                        .replyCount(tuple.get(replyCount))
                        .likeCount(tuple.get(likeCount))
                        .interestState(tuple.get(interest.state))
                        .score(tuple.get(score1.score))
                        .isLiked(tuple.get(isLiked) != null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public DetailDto.CommentDetail getComment(Long contentId, Long commentUserId, Long contextUserId) {
        NumberPath<Long> replyCount = Expressions.numberPath(Long.class, "replyCount");
        NumberPath<Long> likeCount = Expressions.numberPath(Long.class, "likeCount");
        NumberPath<Long> isLiked = Expressions.numberPath(Long.class, "isLiked");

        Tuple result = query
                .select(
                        user.id,
                        user.name,
                        comment.description,
                        comment.containsSpoiler,
                        as(select(reply.id.count())
                                .from(reply)
                                .where(reply.comment.id.contentId.eq(comment.id.contentId),
                                        reply.comment.id.userId.eq(comment.id.userId)), replyCount),
                        as(select(commentLike.id.likeUserId.count())
                                .from(commentLike)
                                .where(commentLike.comment.id.contentId.eq(comment.id.contentId),
                                        commentLike.comment.id.userId.eq(comment.id.userId)), likeCount),
                        interest.state,
                        score1.score,
                        as(select(commentLike.id.likeUserId)
                                .from(commentLike)
                                .where(commentLike.comment.id.contentId.eq(comment.id.contentId),
                                        commentLike.comment.id.userId.eq(comment.id.userId),
                                        commentLike.id.likeUserId.eq(contextUserId)), isLiked)
                )
                .from(comment)
                .join(comment.content, content)
                .join(comment.user, user)
                .leftJoin(interest).on(
                        interest.id.userId.eq(user.id),
                        interest.id.contentId.eq(content.id)
                )
                .leftJoin(score1).on(
                        score1.id.userId.eq(user.id),
                        score1.id.contentId.eq(content.id)
                )
                .where(
                        comment.id.contentId.eq(contentId),
                        comment.id.userId.eq(commentUserId)
                )
                .fetchOne();

        if (result == null) return null;

        return DetailDto.CommentDetail.builder()
                .userId(result.get(user.id))
                .userName(result.get(user.name))
                .description(result.get(comment.description))
                .isSpoiler(result.get(comment.containsSpoiler))
                .replyCount(result.get(replyCount))
                .likeCount(result.get(likeCount))
                .interestState(result.get(interest.state))
                .score(result.get(score1.score))
                .isLiked(result.get(isLiked) != null)
                .build();
    }
}
