package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.domain.Tag;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class TagRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private TagRepository tagRepository;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    public void setup() {

    }

    @Test
    public void findListIn_Correct_ReturnList() throws Exception {
        // given
        Tag expected1 = Tag.builder()
                .description("tag1")
                .build();

        Tag expected2 = Tag.builder()
                .description("tag2")
                .build();

        Tag expected3 = Tag.builder()
                .description("tag3")
                .build();

        em.persist(expected1);
        em.persist(expected2);
        em.persist(expected3);

        Set<Long> ids = new HashSet<>(Arrays.asList(expected1.getId(), expected2.getId(), expected3.getId()));

        // when
        List<Tag> actualList = tagRepository.findListIn(ids);

        // then
        assertThat(actualList).hasSize(3);
    }

    @Test
    public void findListIn_NullSet_ReturnEmptyList() throws Exception {
        // given
        Set<Long> ids = null;

        // when
        List<Tag> actualList = tagRepository.findListIn(ids);

        // then
        assertThat(actualList).isNotNull().hasSize(0);
    }

    @Test
    public void searchWithPaging_WithQuery_PagingQueriedList() throws Exception {
        // given
        Tag expected1 = Tag.builder()
                .description("aaatag1")
                .build();

        Tag expected2 = Tag.builder()
                .description("bbbtag2")
                .build();

        Tag expected3 = Tag.builder()
                .description("aabtag3")
                .build();

        em.persist(expected1);
        em.persist(expected2);
        em.persist(expected3);

        // when
        List<Tag> actualList = tagRepository.searchWithPaging("aa", 1, 5);

        // then
        assertThat(actualList).hasSize(2);
    }

    @Test
    public void searchWithPaging_WithoutQuery_PagingOnlyList() throws Exception {
        // given
        Tag expected1 = Tag.builder()
                .description("tag1")
                .build();

        Tag expected2 = Tag.builder()
                .description("tag2")
                .build();

        Tag expected3 = Tag.builder()
                .description("tag3")
                .build();

        em.persist(expected1);
        em.persist(expected2);
        em.persist(expected3);

        // when
        List<Tag> actualList = tagRepository.searchWithPaging("", 1, 5);

        // then
        assertThat(actualList).hasSize(3);
    }
}