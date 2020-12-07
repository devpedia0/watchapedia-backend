package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.*;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class ContentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ContentRepository contentRepository;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @Test
    public void findById_CertainType_ThatType() throws Exception {
        // given
        Movie expected = Movie.builder()
                .mainTitle("main")
                .posterImage(null)
                .isWatchaContent(false)
                .isNetflixContent(false)
                .totalAudience(1000L)
                .runningTimeInMinutes(100)
                .bookRate(10.1)
                .originTitle("title")
                .countryCode("KR")
                .productionDate(LocalDate.of(2020, 1, 20))
                .description("desc")
                .category("horror")
                .build();

        em.persist(expected);

        // when
        Movie actual = contentRepository.findById(Movie.class, expected.getId());

        // then
        assertThat(actual.getId()).isEqualTo(expected.getId());
    }

    @Test
    public void findById_DifferentType_ReturnNull() throws Exception {
        // given
        Movie expected = Movie.builder()
                .mainTitle("main")
                .posterImage(null)
                .isWatchaContent(false)
                .isNetflixContent(false)
                .totalAudience(1000L)
                .runningTimeInMinutes(100)
                .bookRate(10.1)
                .originTitle("title")
                .countryCode("KR")
                .productionDate(LocalDate.of(2020, 1, 20))
                .description("desc")
                .category("horror")
                .build();

        em.persist(expected);

        // when
        Book actual = contentRepository.findById(Book.class, expected.getId());

        // then
        assertThat(actual).isNull();
    }

    @Test
    public void findListIn_CertainType_ThatTypeList() throws Exception {
        // given
        Movie expected1 = Movie.builder()
                .mainTitle("main")
                .posterImage(null)
                .isWatchaContent(false)
                .isNetflixContent(false)
                .totalAudience(1000L)
                .runningTimeInMinutes(100)
                .bookRate(10.1)
                .originTitle("title")
                .countryCode("KR")
                .productionDate(LocalDate.of(2020, 1, 20))
                .description("desc")
                .category("horror")
                .build();

        Movie expected2 = Movie.builder()
                .mainTitle("main2")
                .posterImage(null)
                .isWatchaContent(false)
                .isNetflixContent(false)
                .totalAudience(1000L)
                .runningTimeInMinutes(100)
                .bookRate(10.1)
                .originTitle("title")
                .countryCode("KR")
                .productionDate(LocalDate.of(2020, 1, 20))
                .description("desc")
                .category("horror")
                .build();

        em.persist(expected1);
        em.persist(expected2);

        Set<Long> ids = new HashSet<>(Arrays.asList(expected1.getId(), expected2.getId()));

        // when
        List<Movie> actualList = contentRepository.findListIn(Movie.class, ids);

        // then
        assertThat(actualList).hasSize(2);
    }
}