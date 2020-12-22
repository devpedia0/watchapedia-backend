package com.devpedia.watchapedia.repository.collection;

import com.devpedia.watchapedia.domain.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long>, CollectionCustomRepository {

}
