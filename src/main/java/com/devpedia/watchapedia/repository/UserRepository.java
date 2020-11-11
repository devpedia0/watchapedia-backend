package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    public void save(User user) {
        em.persist(user);
    }

    public User findById(Long id) {
        return em.find(User.class, id);
    }

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
}
