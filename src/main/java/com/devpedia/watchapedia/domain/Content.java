package com.devpedia.watchapedia.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@DiscriminatorColumn(name = "dtype")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Content {

    @Id @GeneratedValue
    @Column(name = "content_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image posterImage;

    @Column(nullable = false)
    private String mainTitle;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDate productionDate;

    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    private List<ContentParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    private List<ContentImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    private List<ContentTag> tags = new ArrayList<>();

    public Content(Image posterImage, String mainTitle, String category, LocalDate productionDate, String description) {
        this.posterImage = posterImage;
        this.mainTitle = mainTitle;
        this.category = category;
        this.productionDate = productionDate;
        this.description = description;
    }

    public void addParticipant(Participant participant, String role, String characterName) {
        if (participant == null || role == null || characterName == null) return;
        ContentParticipant cp = ContentParticipant.builder()
                .content(this)
                .participant(participant)
                .characterName(characterName)
                .role(role)
                .build();
        this.addContentParticipant(cp);
    }

    public void addTag(Tag tag) {
        if (tag == null) return;
        ContentTag ct = ContentTag.builder()
                .content(this)
                .tag(tag)
                .build();
        this.addContentTag(ct);
    }

    // 연관관계 메서드
    public void addContentParticipant(ContentParticipant contentParticipant) {
        this.participants.add(contentParticipant);
        contentParticipant.setContent(this);
    }

    public void addContentImage(ContentImage contentImage) {
        this.images.add(contentImage);
        contentImage.setContent(this);
    }

    public void addContentTag(ContentTag contentTag) {
        this.tags.add(contentTag);
        contentTag.setContent(this);
    }
}
