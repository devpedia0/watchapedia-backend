package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.builder.ContentMother;
import com.devpedia.watchapedia.builder.ParticipantMother;
import com.devpedia.watchapedia.builder.UserMother;
import com.devpedia.watchapedia.config.TestConfig;
import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.repository.participant.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase
@Import(TestConfig.class)
class ParticipantRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ParticipantRepository participantRepository;

    @Test
    public void getContentScore_DifferentScore_ReturnScore() throws Exception {
        // given
        Movie movie1 = ContentMother.movie().build();
        Movie movie2 = ContentMother.movie().build();
        Movie movie3 = ContentMother.movie().build();

        Participant participant1 = ParticipantMother.defaultParticipant("배우").build();
        Participant participant2 = ParticipantMother.defaultParticipant("배우").build();

        movie1.addParticipant(participant1, "role", "name");

        movie2.addParticipant(participant2, "role", "name");
        movie3.addParticipant(participant2, "role", "name");

        em.persist(participant1);
        em.persist(participant2);
        em.persist(movie1);
        em.persist(movie2);
        em.persist(movie3);

        // when
        Participant mostFamous = participantRepository.findMostFamous(ContentTypeParameter.MOVIES, "배우");

        // then
        assertThat(mostFamous).isNotNull();
        assertThat(mostFamous.getId()).isEqualTo(participant2.getId());
    }
}