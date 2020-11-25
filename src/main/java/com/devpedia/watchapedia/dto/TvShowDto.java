package com.devpedia.watchapedia.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class TvShowDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TvShowInsertRequest {
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
        @NotNull
        private Boolean isWatchaContent;
        @NotNull
        private Boolean isNetflixContent;

        private List<ParticipantDto.ParticipantRole> roles;

        private List<Long> tags;
    }
}
