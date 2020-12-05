package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.Image;
import com.devpedia.watchapedia.domain.Movie;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

public class MovieDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovieInsertRequest {
        @NotBlank
        private String mainTitle;
        @NotBlank
        private String category;
        @NotBlank
        private LocalDate productionDate;
        @NotBlank
        private String description;
        @NotBlank
        private String originTitle;
        @NotBlank
        private String countryCode;
        @Positive
        private Integer runningTimeInMinutes;
        @NotNull
        private Boolean isWatchaContent;
        @NotNull
        private Boolean isNetflixContent;

        private Double bookRate;

        private Long totalAudience;

        private List<ParticipantDto.ParticipantRole> roles;

        private List<Long> tags;

        public Movie toEntity() {
            return Movie.builder()
                    .posterImage(null)
                    .mainTitle(this.mainTitle)
                    .category(this.category)
                    .description(this.description)
                    .productionDate(this.productionDate)
                    .countryCode(this.countryCode)
                    .originTitle(this.originTitle)
                    .runningTimeInMinutes(this.runningTimeInMinutes)
                    .bookRate(this.bookRate)
                    .totalAudience(this.totalAudience)
                    .isNetflixContent(this.isNetflixContent)
                    .isWatchaContent(this.isWatchaContent)
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovieInfo {

        private String mainTitle;

        private String category;

        private LocalDate productionDate;

        private String description;

        private String originTitle;

        private String countryCode;

        private Integer runningTimeInMinutes;

        private Boolean isWatchaContent;

        private Boolean isNetflixContent;

        private Double bookRate;

        private Long totalAudience;

        private Movie movie;
        public MovieInfo(Movie movie) {
            this.movie = movie;
        }

    }
}
