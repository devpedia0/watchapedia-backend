package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.Participant;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class ParticipantRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ParticipantRepository participantRepository;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    public void setup() {

    }

    @Test
    public void findListIn_Correct_ReturnList() throws Exception {
        // given
        Participant participant1 = Participant.builder()
                .name("p1")
                .description("desc1")
                .build();

        Participant participant2 = Participant.builder()
                .name("p2")
                .description("desc2")
                .build();

        Participant participant3 = Participant.builder()
                .name("p2")
                .description("desc2")
                .build();

        em.persist(participant1);
        em.persist(participant2);
        em.persist(participant3);

        Set<Long> ids = new HashSet<>(Arrays.asList(participant1.getId(), participant2.getId(), participant3.getId()));

        // when
        List<Participant> list = participantRepository.findListIn(ids);

        // then
        assertThat(list).hasSize(3);
    }

    @Test
    public void findListIn_NullSet_ReturnEmptyList() throws Exception {
        // given
        Set<Long> ids = null;

        // when
        List<Participant> list = participantRepository.findListIn(ids);

        // then
        assertThat(list).isNotNull().hasSize(0);
    }

    @Test
    public void searchWithPaging_WithQuery_PagingQueriedList() throws Exception {
        // given
        Participant participant1 = Participant.builder()
                .name("aaa")
                .description("desc1")
                .build();

        Participant participant2 = Participant.builder()
                .name("bbb")
                .description("desc2")
                .build();

        Participant participant3 = Participant.builder()
                .name("aab")
                .description("desc2")
                .build();

        em.persist(participant1);
        em.persist(participant2);
        em.persist(participant3);

        // when
        List<Participant> list = participantRepository.searchWithPaging("aa", 1, 5);

        // then
        assertThat(list).hasSize(2);
    }

    @Test
    public void searchWithPaging_WithoutQuery_PagingOnlyList() throws Exception {
        // given
        Participant participant1 = Participant.builder()
                .name("aaa")
                .description("desc1")
                .build();

        Participant participant2 = Participant.builder()
                .name("bbb")
                .description("desc2")
                .build();

        Participant participant3 = Participant.builder()
                .name("aab")
                .description("desc2")
                .build();

        em.persist(participant1);
        em.persist(participant2);
        em.persist(participant3);

        // when
        List<Participant> list = participantRepository.searchWithPaging("", 1, 5);

        // then
        assertThat(list).hasSize(3);
    }
}