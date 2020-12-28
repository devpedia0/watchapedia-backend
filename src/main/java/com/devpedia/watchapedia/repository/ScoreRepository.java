package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Comment;
import com.devpedia.watchapedia.domain.Score;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository extends JpaRepository<Score, Score.ScoreId> {
}
