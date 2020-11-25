package com.devpedia.watchapedia.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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

    @Column(nullable = false)
    private String job;

    private String description;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL)
    private List<ContentParticipant> contentList = new ArrayList<>();

    @Builder
    public Participant(String name, Image profileImage, String job, String description) {
        this.name = name;
        this.profileImage = profileImage;
        this.job = job;
        this.description = description;
    }

    public void updateInfo(String name, String job, String description) {
        if (name != null && !StringUtils.isBlank(name)) this.name = name;
        if (job != null && !StringUtils.isBlank(name)) this.job = job;
        if (description != null) this.description = description;
    }
}
