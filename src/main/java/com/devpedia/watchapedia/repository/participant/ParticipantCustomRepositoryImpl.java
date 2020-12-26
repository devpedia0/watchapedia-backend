package com.devpedia.watchapedia.repository.participant;

import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;

import java.util.List;

import static com.devpedia.watchapedia.domain.QContent.content;
import static com.devpedia.watchapedia.domain.QContentParticipant.contentParticipant;
import static com.devpedia.watchapedia.domain.QParticipant.participant;

@RequiredArgsConstructor
public class ParticipantCustomRepositoryImpl implements ParticipantCustomRepository {

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

    @Override
    public List<Participant> findContentParticipantHasJob(Long contentId, String job) {
        return query
                .select(participant)
                .from(contentParticipant)
                .join(contentParticipant.participant, participant)
                .join(contentParticipant.content, content)
                .where(
                        content.id.eq(contentId),
                        participant.job.eq(job)
                )
                .fetch();
    }
}
