package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLike.CommentLikeId>  {
}
