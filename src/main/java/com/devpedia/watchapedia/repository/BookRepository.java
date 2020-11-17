package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Book;
import com.devpedia.watchapedia.domain.ContentParticipant;
import com.devpedia.watchapedia.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final EntityManager em;

    public void save(Book book) {
        em.persist(book);
    }
}
