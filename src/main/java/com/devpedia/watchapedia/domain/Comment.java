package com.devpedia.watchapedia.domain;

import lombok.*;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "delete_yn = 'N'")
public class Comment extends BaseEntity {

    @EmbeddedId
    private CommentId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("contentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "spoiler_yn", nullable = false)
    private Boolean containsSpoiler;

    @Column(name = "delete_yn", nullable = false)
    private Boolean isDeleted;

    @Builder
    public Comment(User user, Content content, String description, Boolean containsSpoiler) {
        this.id = new CommentId(user.getId(), content.getId());
        this.user = user;
        this.content = content;
        this.description = description;
        this.containsSpoiler = containsSpoiler;
        this.isDeleted = false;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentId implements Serializable {
        private Long userId;
        private Long contentId;
    }
}
