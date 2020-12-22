package com.devpedia.watchapedia.repository.content;

import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContentRepository extends JpaRepository<Content, Long>, ContentCustomRepository {

    @Query("select count(s) from Score s")
    Long countTotalScores();
}
