package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.domain.enums.AccessRange;
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

import javax.persistence.PersistenceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @Test
    public void save_CorrectArgs_SaveUser() throws Exception {
        //given
        User expectedUser = User.builder()
                .email("aaa@bb.com")
                .password("1234")
                .name("testName")
                .countryCode("KR")
                .build();

        userRepository.save(expectedUser);

        //when
        User actualUser = userRepository.findById(expectedUser.getId());

        //then
        assertThat(actualUser.getEmail()).isEqualTo("aaa@bb.com");
        assertThat(actualUser.getPassword()).isEqualTo("1234");
        assertThat(actualUser.getName()).isEqualTo("testName");
        assertThat(actualUser.getCountryCode()).isEqualTo("KR");
        assertThat(actualUser.getAccessRange()).isEqualTo(AccessRange.PUBLIC);
        assertThat(actualUser.getRoles()).contains("USER");
        assertThat(actualUser.getIsDeleted()).isFalse();
        assertThat(actualUser.getIsEmailAgreed()).isFalse();
        assertThat(actualUser.getIsSmsAgreed()).isFalse();
        assertThat(actualUser.getIsPushAgreed()).isFalse();
    }

    @Test
    public void save_EmptyArgs_ThrowException() throws Exception {
        //given
        User expectedUser = User.builder()
//                .email("aaa@bb.com")
//                .password("1111")
//                .name("testName")
//                .countryCode("KR")
                .build();

        //when
        userRepository.save(expectedUser);

        //then
        assertThatThrownBy(() -> em.flush())
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    public void findById_ExistId_ReturnUser() throws Exception {
        //given
        User expectedUser = User.builder()
                .email("aaa@bb.com")
                .password("1111")
                .name("testName")
                .countryCode("KR")
                .build();

        userRepository.save(expectedUser);

        //when
        User actualUser = userRepository.findById(expectedUser.getId());

        //then
        assertThat(actualUser).isNotNull();
    }

    @Test
    public void findById_NotExistId_ReturnNull() throws Exception {
        //given
        User expectedUser = User.builder()
                .email("aaa@bb.com")
                .password("1111")
                .name("testName")
                .countryCode("KR")
                .build();

        userRepository.save(expectedUser);

        //when
        User actualUser = userRepository.findById(10000L);

        //then
        assertThat(actualUser).isNull();
    }

    @Test
    public void findByEmail_ExistId_ReturnUser() throws Exception {
        //given
        User expectedUser = User.builder()
                .email("aaa@bb.com")
                .password("1111")
                .name("testName")
                .countryCode("KR")
                .build();

        userRepository.save(expectedUser);

        //when
        User actualUser = userRepository.findByEmail("aaa@bb.com");

        //then
        assertThat(actualUser).isNotNull();
    }

    @Test
    public void findByEmail_NotExistId_ReturnNull() throws Exception {
        //given
        User expectedUser = User.builder()
                .email("aaa@bb.com")
                .password("1111")
                .name("testName")
                .countryCode("KR")
                .build();

        userRepository.save(expectedUser);

        //when
        User actualUser = userRepository.findByEmail("yyy@zz.com");

        //then
        assertThat(actualUser).isNull();
    }

}