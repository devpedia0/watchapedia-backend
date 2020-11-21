package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Movie;
import com.devpedia.watchapedia.domain.TvShow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class TvShowRepository {

    private final EntityManager em;

    public void save(TvShow tvShow) {
        em.persist(tvShow);
    }
}
