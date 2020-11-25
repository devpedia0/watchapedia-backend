package com.devpedia.watchapedia.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike {

    @EmbeddedId
    private CommentLikeId id;

    @MapsId("likeUserId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "like_user_id", referencedColumnName = "user_id")
    private User user;

    @MapsId("commentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "comment_user_id", referencedColumnName = "user_id"),
            @JoinColumn(name = "content_id", referencedColumnName = "content_id")
    })
    private Comment comment;

    @Builder
    public CommentLike(User user, Comment comment) {
        this.id = new CommentLikeId(comment.getId(), user.getId());
        this.user = user;
        this.comment = comment;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentLikeId implements Serializable {
        private Comment.CommentId commentId;
        private Long likeUserId;
    }
}
