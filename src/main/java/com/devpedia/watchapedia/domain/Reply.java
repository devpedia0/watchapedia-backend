package com.devpedia.watchapedia.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reply extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "reply_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "comment_user_id", referencedColumnName = "user_id"),
            @JoinColumn(name = "content_id", referencedColumnName = "content_id")
    })
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_user_id", referencedColumnName = "user_id")
    private User user;

    @Column(nullable = false)
    private String description;

    public Reply(Comment comment, User user, String description) {
        this.comment = comment;
        this.user = user;
        this.description = description;
    }
}
