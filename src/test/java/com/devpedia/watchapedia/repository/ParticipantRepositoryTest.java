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
        Participant expected1 = Participant.builder()
                .name("p1")
                .job("j1")
                .description("desc1")
                .build();

        Participant expected2 = Participant.builder()
                .name("p2")
                .job("j2")
                .description("desc2")
                .build();

        Participant expected3 = Participant.builder()
                .name("p3")
                .job("j3")
                .description("desc3")
                .build();

        em.persist(expected1);
        em.persist(expected2);
        em.persist(expected3);

        Set<Long> ids = new HashSet<>(Arrays.asList(expected1.getId(), expected2.getId(), expected3.getId()));

        // when
        List<Participant> actualList = participantRepository.findListIn(ids);

        // then
        assertThat(actualList).hasSize(3);
    }

    @Test
    public void findListIn_NullSet_ReturnEmptyList() throws Exception {
        // given
        Set<Long> ids = null;

        // when
        List<Participant> actualList = participantRepository.findListIn(ids);

        // then
        assertThat(actualList).isNotNull().hasSize(0);
    }

    @Test
    public void searchWithPaging_WithQuery_PagingQueriedList() throws Exception {
        // given
        Participant expected1 = Participant.builder()
                .name("aaa")
                .job("j1")
                .description("desc1")
                .build();

        Participant expected2 = Participant.builder()
                .name("bbb")
                .job("j2")
                .description("desc2")
                .build();

        Participant expected3 = Participant.builder()
                .name("aab")
                .job("j3")
                .description("desc2")
                .build();

        em.persist(expected1);
        em.persist(expected2);
        em.persist(expected3);

        // when
        List<Participant> actualList = participantRepository.searchWithPaging("aa", 1, 5);

        // then
        assertThat(actualList).hasSize(2);
    }

    @Test
    public void searchWithPaging_WithoutQuery_PagingOnlyList() throws Exception {
        // given
        Participant expected1 = Participant.builder()
                .name("aaa")
                .job("j1")
                .description("desc1")
                .build();

        Participant expected2 = Participant.builder()
                .name("bbb")
                .job("j2")
                .description("desc2")
                .build();

        Participant expected3 = Participant.builder()
                .name("aab")
                .job("j3")
                .description("desc2")
                .build();

        em.persist(expected1);
        em.persist(expected2);
        em.persist(expected3);

        // when
        List<Participant> actualList = participantRepository.searchWithPaging("", 1, 5);

        // then
        assertThat(actualList).hasSize(3);
    }
}