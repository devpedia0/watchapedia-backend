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
@DiscriminatorValue("B")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends Content {

    @Column(nullable = false)
    private String subtitle;

    @Column(nullable = false)
    private Integer page;

    @Column(columnDefinition = "TEXT")
    private String contents;

    @Column(columnDefinition = "TEXT")
    private String elaboration;

    @Builder
    public Book(Image posterImage, String mainTitle, String category, LocalDate productionDate, String description,
                String subtitle, Integer page, String contents, String elaboration) {
        super(posterImage, mainTitle, category, productionDate, description);
        this.subtitle = subtitle;
        this.page = page;
        this.contents = contents;
        this.elaboration = elaboration;
    }
}
