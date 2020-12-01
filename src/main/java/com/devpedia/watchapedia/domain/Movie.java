package com.devpedia.watchapedia.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("M")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie extends Content {

    @Column(nullable = false)
    private String originTitle;

    @Column(nullable = false)
    private String countryCode;

    @Column(name = "running_time", nullable = false)
    private Integer runningTimeInMinutes;

    @Column(name = "watcha_yn", nullable = false)
    private Boolean isWatchaContent;

    @Column(name = "netflix_yn", nullable = false)
    private Boolean isNetflixContent;

    private Double bookRate;

    private Long totalAudience;

    @Builder
    public Movie(Ranking ranking,Image posterImage, String mainTitle, String category, LocalDate productionDate, String description,
                 String originTitle, String countryCode, Integer runningTimeInMinutes, Boolean isWatchaContent,
                 Boolean isNetflixContent, Double bookRate, Long totalAudience) {
        super(ranking, posterImage, mainTitle, category, productionDate, description);
        this.originTitle = originTitle;
        this.countryCode = countryCode;
        this.runningTimeInMinutes = runningTimeInMinutes;
        this.isWatchaContent = isWatchaContent;
        this.isNetflixContent = isNetflixContent;
        this.bookRate = bookRate;
        this.totalAudience = totalAudience;
    }
}
