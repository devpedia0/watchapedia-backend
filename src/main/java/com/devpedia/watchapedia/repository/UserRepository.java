package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    /**
     * 유저 저장
     * @param user 유저
     */
    public void save(User user) {
        em.persist(user);
    }

    /**
     * PK로 유저 조회
     * @param id user_id
     * @return 조회된 유저 없으면 null
     */
    public User findById(Long id) {
        return em.find(User.class, id);
    }

    /**
     * 이메일로 유저를 조회한다.
     * 이메일은 unique 이므로 2건이상 조회되지 않는다.
     * @param email 검색 이메일
     * @return 유저
     */
    public User findByEmail(String email) {
        List<User> result = em.createQuery(
                "select u " +
                        "from User u " +
                        "where u.email = :email", User.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * 컨텐츠별 유저가 매긴 평점 개수와 보고싶어요 개수를 구한다.
     * @param id user_id
     * @return 유저의 컨텐츠 별 평점 및 보고싶어요 개수
     */
    public UserDto.UserRatingAndWishContent getRatingAndWishCounts(Long id) {
        List<Object[]> resultList = em.createNativeQuery(
                "select a.dtype as type, " +
                        "    count(case when b.user_id is not null then 1 end) as ratingCount, " +
                        "    count(case when i.user_id is not null then 1 end) as wishCount " +
                        "from content a " +
                        "left outer join score b on a.content_id = b.content_id " +
                        "   and b.user_id = :id " +
                        "left outer join interest i on a.content_id = i.content_id " +
                        "   and i.user_id = :id " +
                        "   and i.state = 1 " +
                        "group by a.dtype")
                .setParameter("id", id)
                .getResultList();

        UserDto.UserRatingAndWishContent contents = UserDto.UserRatingAndWishContent.builder().build();
        for (Object[] objects : resultList) {
            UserDto.RatingAndWishCount ratingAndWishCount = UserDto.RatingAndWishCount.builder()
                    .ratingCount(((BigInteger) objects[1]).intValue())
                    .wishCount(((BigInteger) objects[2]).intValue())
                    .build();
            if ((objects[0]).equals("M")) {
                contents.setMovie(ratingAndWishCount);
            } else if ((objects[0]).equals("B")) {
                contents.setBook(ratingAndWishCount);
            } else {
                contents.setTvShow(ratingAndWishCount);
            }
        }
        return contents;
    }
}
