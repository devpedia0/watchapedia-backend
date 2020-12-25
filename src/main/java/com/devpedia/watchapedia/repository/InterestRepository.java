package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Interest;
import com.devpedia.watchapedia.domain.Score;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest, Interest.InterestId> {
}
