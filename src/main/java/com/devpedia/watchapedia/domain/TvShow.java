package com.devpedia.watchapedia.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("S")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvShow extends Content {

    @Column(nullable = false)
    private String originTitle;

    @Column(nullable = false)
    private String countryCode;

    @Column(name = "watcha_yn", nullable = false)
    private Boolean isWatchaContent;

    @Column(name = "netflix_yn", nullable = false)
    private Boolean isNetflixContent;

    @Builder
    public TvShow(Image posterImage, String mainTitle, String category, LocalDate productionDate, String description,
                  String originTitle, String countryCode, Boolean isWatchaContent, Boolean isNetflixContent) {
        super(posterImage, mainTitle, category, productionDate, description);
        this.originTitle = originTitle;
        this.countryCode = countryCode;
        this.isWatchaContent = isWatchaContent;
        this.isNetflixContent = isNetflixContent;
    }
}
