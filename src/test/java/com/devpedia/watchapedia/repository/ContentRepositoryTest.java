package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.builder.CollectionMother;
import com.devpedia.watchapedia.builder.ContentMother;
import com.devpedia.watchapedia.builder.ParticipantMother;
import com.devpedia.watchapedia.builder.UserMother;
import com.devpedia.watchapedia.config.TestConfig;
import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.repository.content.ContentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase
@Import(TestConfig.class)
class ContentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ContentRepository contentRepository;

    @Test
    public void getContentsScoreIsGreaterThan_ScoreGt4_ReturnContents() throws Exception {
        // given
        User user = UserMother.defaultUser().build();

        Movie movie1 = ContentMother.movie().build();
        Movie movie2 = ContentMother.movie().build();
        Movie movie3 = ContentMother.movie().build();

        Score score1 = Score.builder().user(user).content(movie1).score(3.0).build();
        Score score2 = Score.builder().user(user).content(movie2).score(4.0).build();
        Score score3 = Score.builder().user(user).content(movie3).score(5.0).build();

        em.persist(user);
        em.persist(score1);
        em.persist(score2);
        em.persist(score3);

        // when
        List<Content> contents = contentRepository.getContentsScoreIsGreaterThan(ContentTypeParameter.MOVIES,
                4.0, 10);

        // then
        assertThat(contents).hasSize(2);
    }

    @Test
    public void getContentScore_DifferentScore_ReturnScore() throws Exception {
        // given
        User user = UserMother.defaultUser().build();

        Movie movie1 = ContentMother.movie().build();
        Movie movie2 = ContentMother.movie().build();
        Movie movie3 = ContentMother.movie().build();

        Score score1 = Score.builder().user(user).content(movie1).score(3.0).build();
        Score score2 = Score.builder().user(user).content(movie2).score(4.0).build();
        Score score3 = Score.builder().user(user).content(movie3).score(5.0).build();

        em.persist(user);
        em.persist(score1);
        em.persist(score2);
        em.persist(score3);

        Set<Long> ids = Set.of(movie1.getId(), movie2.getId(), movie3.getId());

        // when
        Map<Long, Double> contentScore = contentRepository.getContentScore(ids);

        // then
        assertThat(contentScore).hasSize(3);
        assertThat(contentScore.get(movie1.getId())).isEqualTo(3.0);
        assertThat(contentScore.get(movie2.getId())).isEqualTo(4.0);
        assertThat(contentScore.get(movie3.getId())).isEqualTo(5.0);
    }

    @Test
    public void getContentsHasParticipant_Exist_ReturnContents() throws Exception {
        // given
        Movie movie1 = ContentMother.movie().build();
        Movie movie2 = ContentMother.movie().build();
        Movie movie3 = ContentMother.movie().build();

        Participant participant = ParticipantMother.defaultParticipant("배우").build();

        movie1.addParticipant(participant, "role", "character");
        movie2.addParticipant(participant, "role", "character");
        movie3.addParticipant(participant, "role", "character");

        em.persist(participant);
        em.persist(movie1);
        em.persist(movie2);
        em.persist(movie3);

        // when
        List<Content> contents = contentRepository.getContentsHasParticipant(ContentTypeParameter.MOVIES,
                participant.getId(), 10);

        // then
        assertThat(contents).hasSize(3);
    }

    @Test
    public void getContentsTagged_Exist_ReturnContents() throws Exception {
        // given
        Movie movie1 = ContentMother.movie().build();
        Movie movie2 = ContentMother.movie().build();
        Movie movie3 = ContentMother.movie().build();

        Tag tag = Tag.builder().description("tag").build();

        movie1.addTag(tag);
        movie2.addTag(tag);
        movie3.addTag(tag);

        em.persist(tag);
        em.persist(movie1);
        em.persist(movie2);
        em.persist(movie3);

        // when
        List<Content> contents = contentRepository.getContentsTagged(ContentTypeParameter.MOVIES,
                tag.getId(), 10);

        // then
        assertThat(contents).hasSize(3);
    }

    @Test
    public void getContentsInCollection_HasContent_ReturnContents() throws Exception {
        // given
        User user = UserMother.defaultUser().build();
        Collection collection = CollectionMother.defaultCollection(user).build();

        CollectionContent cc1 = CollectionContent.builder()
                .collection(collection)
                .content(ContentMother.movie().build())
                .build();

        CollectionContent cc2 = CollectionContent.builder()
                .collection(collection)
                .content(ContentMother.movie().build())
                .build();

        em.persist(user);
        em.persist(collection);
        em.persist(cc1);
        em.persist(cc2);

        // when
        List<Content> contents = contentRepository.getContentsInCollection(collection.getId(),
                PageRequest.of(0, 10));

        // then
        assertThat(contents).hasSize(2);
    }

    @Test
    @Sql({"classpath:sql/default_contents.sql", "classpath:sql/trending_words.sql"})
    public void getTrendingWords_Exist_ReturnWords() throws Exception {
        // given

        // when
        List<String> words = contentRepository.getTrendingWords(2);

        // then
        assertThat(words).hasSize(2);
        assertThat(words.get(0)).isEqualTo("Movie Title1");
        assertThat(words.get(1)).isEqualTo("Movie Title2");
    }

}