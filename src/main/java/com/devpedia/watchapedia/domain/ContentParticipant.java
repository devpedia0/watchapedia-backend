package com.devpedia.watchapedia.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"participant_id", "content_id", "role"})})
public class ContentParticipant {

    @Id
    @GeneratedValue
    @Column(name = "content_participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @Column(nullable = false)
    private String role;

    private String characterName;

    @Builder
    public ContentParticipant(Participant participant, Content content, String role, String characterName) {
        this.participant = participant;
        this.content = content;
        this.role = role;
        this.characterName = characterName;
    }

    public void setContent(Content content) {
        this.content = content;
    }
}
