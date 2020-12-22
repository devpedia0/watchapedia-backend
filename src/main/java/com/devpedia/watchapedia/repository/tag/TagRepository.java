package com.devpedia.watchapedia.repository.tag;

import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.domain.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long>, TagCustomRepository {

    List<Tag> findByDescriptionContaining(String description, Pageable pageable);

    @Query("select t from Tag t order by function('rand')")
    List<Tag> findByRandom(Pageable pageable);
}
