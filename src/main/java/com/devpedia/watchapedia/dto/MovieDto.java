package com.devpedia.watchapedia.dto;

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

        private List<ParticipantDto.ParticipantRole> roleList;

        private List<Long> tagList;
    }
}
