package com.devpedia.watchapedia.repository.user;

import com.devpedia.watchapedia.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>, UserCustomRepository {

    User findFirstByEmail(String email);

    List<User> findByNameContaining(String name, Pageable pageable);
}
