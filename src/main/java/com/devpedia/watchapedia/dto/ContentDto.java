package com.devpedia.watchapedia.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

public class ContentDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommonContentInfo {

        private Long id;

        private String mainTitle;

        private String contentType;

        private LocalDate productionDate;

        private String posterImagePath;
    }
}
