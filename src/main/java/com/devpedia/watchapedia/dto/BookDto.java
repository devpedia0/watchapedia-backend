package com.devpedia.watchapedia.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.List;

public class BookDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookInsertRequest {
        @NotBlank
        private String mainTitle;
        @NotBlank
        private String category;
        @NotBlank
        private LocalDate productionDate;
        @NotBlank
        private String description;
        @NotBlank
        private String subtitle;
        @PositiveOrZero
        private Integer page;

        private String contents;

        private String elaboration;

        private List<ParticipantDto.ParticipantRole> roles;

        private List<Long> tags;
    }
}
