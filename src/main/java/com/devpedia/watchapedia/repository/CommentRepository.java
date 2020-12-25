package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Comment.CommentId> {
}
