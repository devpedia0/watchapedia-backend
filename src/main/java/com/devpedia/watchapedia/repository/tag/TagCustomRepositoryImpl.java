package com.devpedia.watchapedia.repository.tag;

import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;

import static com.devpedia.watchapedia.domain.QContent.content;
import static com.devpedia.watchapedia.domain.QContentParticipant.contentParticipant;
import static com.devpedia.watchapedia.domain.QParticipant.participant;

@RequiredArgsConstructor
public class TagCustomRepositoryImpl implements TagCustomRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public Participant findMostFamous(ContentTypeParameter type, String job) {
        return query
                .select(participant)
                .from(contentParticipant)
                .join(contentParticipant.participant, participant)
                .join(contentParticipant.content, content)
                .where(
                        participant.job.eq(job),
                        content.dtype.eq(type.getDtype())
                )
                .groupBy(contentParticipant.participant.id)
                .orderBy(contentParticipant.participant.id.count().desc())
                .fetchFirst();
    }
}
