package com.devpedia.watchapedia.repository.content;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.devpedia.watchapedia.domain.QCollection.collection;
import static com.devpedia.watchapedia.domain.QCollectionContent.collectionContent;
import static com.devpedia.watchapedia.domain.QComment.*;
import static com.devpedia.watchapedia.domain.QContent.content;
import static com.devpedia.watchapedia.domain.QContentParticipant.*;
import static com.devpedia.watchapedia.domain.QContentTag.*;
import static com.devpedia.watchapedia.domain.QImage.*;
import static com.devpedia.watchapedia.domain.QScore.*;
import static com.querydsl.core.types.ExpressionUtils.*;
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
}
