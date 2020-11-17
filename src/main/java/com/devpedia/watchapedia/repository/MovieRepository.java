package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Book;
import com.devpedia.watchapedia.domain.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class MovieRepository {

    private final EntityManager em;

    public void save(Movie movie) {
        em.persist(movie);
    }
}
