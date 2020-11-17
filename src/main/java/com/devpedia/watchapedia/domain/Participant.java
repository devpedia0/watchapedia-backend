package com.devpedia.watchapedia.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant {

    @Id @GeneratedValue
    @Column(name = "participant_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image profileImage;

    private String description;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<ContentParticipant> contentList = new ArrayList<>();

    @Builder
    public Participant(String name, Image profileImage, String description) {
        this.name = name;
        this.profileImage = profileImage;
        this.description = description;
    }

    public void updateInfo(String name, String description) {
        if (name != null && !name.isBlank()) this.name = name;
        if (description != null) this.description = description;
    }
}
