package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.builder.CollectionMother;
import com.devpedia.watchapedia.builder.ContentMother;
import com.devpedia.watchapedia.builder.UserMother;
import com.devpedia.watchapedia.config.TestConfig;
import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.CollectionContent;
import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.repository.collection.CollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase
@Import(TestConfig.class)
class CollectionRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CollectionRepository collectionRepository;

    @Test
    public void countContentById_HasContent_ReturnCount() throws Exception {
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
        Long count = collectionRepository.countContentById(collection.getId());

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    public void countContentById_HasNoContent_ReturnZero() throws Exception {
        // given
        User user = UserMother.defaultUser().build();
        Collection collection = CollectionMother.defaultCollection(user).build();

        em.persist(user);
        em.persist(collection);

        // when
        Long count = collectionRepository.countContentById(collection.getId());

        // then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @Sql("classpath:sql/admin_user.sql")
    public void getAward_HasContent_ReturnAward() throws Exception {
        // given
        User user = em.find(User.class, 1L);
        Collection collection1 = CollectionMother.defaultCollection(user).build();
        Collection collection2 = CollectionMother.defaultCollection(user).build();

        CollectionContent cc1 = CollectionContent.builder()
                .collection(collection1)
                .content(ContentMother.movie().build())
                .build();

        CollectionContent cc2 = CollectionContent.builder()
                .collection(collection2)
                .content(ContentMother.movie().build())
                .build();

        em.persist(user);
        em.persist(collection1);
        em.persist(collection2);
        em.persist(cc1);
        em.persist(cc2);

        // when
        List<Collection> award = collectionRepository.getAward(ContentTypeParameter.MOVIES);

        // then
        assertThat(award).hasSize(2);
    }

    @Test
    @Sql("classpath:sql/admin_user.sql")
    public void getRandom_HasContent_ReturnCollection() throws Exception {
        // given
        User user = em.find(User.class, 1L);
        Collection collection1 = CollectionMother.defaultCollection(user).build();
        Collection collection2 = CollectionMother.defaultCollection(user).build();

        CollectionContent cc1 = CollectionContent.builder()
                .collection(collection1)
                .content(ContentMother.movie().build())
                .build();

        CollectionContent cc2 = CollectionContent.builder()
                .collection(collection2)
                .content(ContentMother.movie().build())
                .build();

        em.persist(user);
        em.persist(collection1);
        em.persist(collection2);
        em.persist(cc1);
        em.persist(cc2);

        // when
        List<Collection> collections = collectionRepository.getRandom(ContentTypeParameter.MOVIES, 10);

        // then
        assertThat(collections).hasSize(2);
    }

}