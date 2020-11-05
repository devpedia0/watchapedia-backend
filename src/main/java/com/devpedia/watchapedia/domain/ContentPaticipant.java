package com.devpedia.watchapedia.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentPaticipant {

    @EmbeddedId
    private ContentPaticipantId id;

    @MapsId("participantId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @MapsId("contentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @Column(nullable = false)
    private String role;

    private String charactorName;

    @Builder
    public ContentPaticipant(Participant participant, Content content, String role, String charactorName) {
        this.id = new ContentPaticipantId(participant.getId(), content.getId());
        this.participant = participant;
        this.content = content;
        this.role = role;
        this.charactorName = charactorName;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentPaticipantId implements Serializable {
        private Long participantId;
        private Long contentId;
    }
}
