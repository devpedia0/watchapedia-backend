package com.devpedia.watchapedia.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

public class TagDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TagInsertRequest {
        @NotBlank
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TagInfo {
        @NotBlank
        private Long id;
        @NotBlank
        private String description;
    }
}
