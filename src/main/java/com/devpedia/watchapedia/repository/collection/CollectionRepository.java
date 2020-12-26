package com.devpedia.watchapedia.repository.collection;

import com.devpedia.watchapedia.domain.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionRepository extends JpaRepository<Collection, Long>, CollectionCustomRepository {

    @Query("select c " +
            "from CollectionContent cc " +
            "join cc.collection c " +
            "where cc.id.contentId = :id " +
            "group by cc.id.collectionId")
    Page<Collection> getContentCollection(@Param("id") Long contentId, Pageable pageable);
}
