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
    private List<ContentPaticipant> participants = new ArrayList<>();

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

    // 연관관계 메서드
    public void addContentPaticipant(ContentPaticipant contentPaticipant) {
        this.participants.add(contentPaticipant);
        contentPaticipant.setContent(this);
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
