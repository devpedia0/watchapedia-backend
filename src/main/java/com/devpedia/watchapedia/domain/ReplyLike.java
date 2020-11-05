package com.devpedia.watchapedia.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplyLike {

    @EmbeddedId
    private ReplyLikeId id;

    @MapsId("likeUserId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "like_user_id", referencedColumnName = "user_id")
    private User user;

    @MapsId("replyId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id")
    private Reply reply;

    @Builder
    public ReplyLike(User user, Reply reply) {
        this.id = new ReplyLikeId(reply.getId(), user.getId());
        this.user = user;
        this.reply = reply;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyLikeId implements Serializable {
        private Long replyId;
        private Long likeUserId;
    }
}
