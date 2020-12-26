package com.devpedia.watchapedia.repository.content;

import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.dto.DetailDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long>, ContentCustomRepository {

    @Query("select count(s) from Score s")
    Long countTotalScores();

    List<Content> findByCategoryContaining(String category, Pageable pageable);

    @Query("select count(c) from Comment c where c.id.contentId = :id")
    Long countComments(@Param("id") Long contentId);

    @Query("select distinct c from ContentParticipant cp join cp.content c where cp.participant.id = :id")
    List<Content> findContentByParticipant(@Param("id") Long id, Pageable pageable);
}
