package com.devpedia.watchapedia.builder;

import com.devpedia.watchapedia.domain.Image;
import com.devpedia.watchapedia.domain.Movie;

import java.time.LocalDate;

public class ContentMother {

    public static Movie.MovieBuilder movie() {
        Image image = Image.builder()
                .name("name")
                .originName("originName")
                .path("/poster")
                .extention("jpg")
                .size(1000L)
                .build();

        return Movie.builder()
                .mainTitle("mainTitle")
                .posterImage(image)
                .isWatchaContent(false)
                .isNetflixContent(false)
                .totalAudience(1000L)
                .runningTimeInMinutes(100)
                .bookRate(0.0)
                .originTitle("originTitle")
                .countryCode("KR")
                .productionDate(LocalDate.of(2020, 1, 20))
                .description("desc")
                .category("cate");
    }
}
