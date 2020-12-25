package com.devpedia.watchapedia.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Score {

    @EmbeddedId
    private ScoreId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("contentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @Column(nullable = false)
    private Double score;

    @Builder
    public Score(User user, Content content, Double score) {
        this.id = new ScoreId(user.getId(), content.getId());
        this.user = user;
        this.content = content;
        this.score = score;
    }

    public void edit(Double score) {
        if (score != null) this.score = score;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreId implements Serializable {
        private Long userId;
        private Long contentId;
    }
}
