package com.devpedia.watchapedia.domain;

import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.domain.enums.InterestStateConverter;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest {

    @EmbeddedId
    private InterestId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("contentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @Convert(converter = InterestStateConverter.class)
    @Column(nullable = false)
    private InterestState state;

    @Builder
    public Interest(User user, Content content, InterestState state) {
        this.id = new InterestId(user.getId(), content.getId());
        this.user = user;
        this.content = content;
        this.state = state;
    }

    public void edit(InterestState state) {
        if (state != null) this.state = state;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterestId implements Serializable {
        private Long userId;
        private Long contentId;
    }
}
