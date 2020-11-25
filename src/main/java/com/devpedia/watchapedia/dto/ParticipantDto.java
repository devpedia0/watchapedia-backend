package com.devpedia.watchapedia.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

public class ParticipantDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantInsertRequest {
        @NotBlank
        private String name;

        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantUpdateRequest {
        private String name;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantRole {
        @NotBlank
        private Long participantId;
        @NotBlank
        private String role;
        @NotBlank
        private String characterName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantInfo {
        @NotBlank
        private Long id;
        @NotBlank
        private String name;

        private String description;

        private String profileImagePath;
    }


}
