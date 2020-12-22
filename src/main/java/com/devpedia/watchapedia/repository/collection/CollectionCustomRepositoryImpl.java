package com.devpedia.watchapedia.repository.collection;

import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.List;

import static com.devpedia.watchapedia.domain.QCollection.collection;
import static com.devpedia.watchapedia.domain.QCollectionContent.collectionContent;
import static com.devpedia.watchapedia.domain.QContent.content;

@RequiredArgsConstructor
public class CollectionCustomRepositoryImpl implements CollectionCustomRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public Long countContentById(Long id) {
        return query
                .select(collectionContent.id.contentId.count())
                .from(collectionContent)
                .where(collectionContent.id.collectionId.eq(id))
                .fetchOne();
    }

    @Override
    public List<Collection> getAward(ContentTypeParameter type) {
        return query
                .select(collection)
                .from(collection)
                .join(collectionContent).on(collection.id.eq(collectionContent.collection.id))
                .join(collectionContent.content, content)
                .where(
                        collection.user.id.eq(1L),
                        content.dtype.eq(type.getDtype())
                )
                .groupBy(collection.id)
                .fetch();
    }
    
    @Override
    public List<Collection> getRandom(ContentTypeParameter type, int size) {
        return query
                .select(collection)
                .from(collectionContent)
                .join(collectionContent.content, content)
                .join(collectionContent.collection, collection).on(content.dtype.eq(type.getDtype()))
                .groupBy(collection.id)
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .limit(size)
                .fetch();
    }
}
