package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.Image;
import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.util.UrlUtil;
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
        @NotBlank
        private String job;

        private String description;

        public Participant toEntity(Image profileImage) {
            return Participant.builder()
                    .profileImage(profileImage)
                    .name(this.name)
                    .job(this.job)
                    .description(this.description)
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantUpdateRequest {
        private String name;
        private String job;
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
        @NotBlank
        private String job;

        private String description;

        private String profileImagePath;

        public ParticipantInfo(Participant participant) {
            this.id = participant.getId();
            this.name = participant.getName();
            this.job = participant.getJob();
            this.description = participant.getDescription();
            this.profileImagePath = participant.getProfileImage() != null
                    ? UrlUtil.getCloudFrontUrl(participant.getProfileImage().getPath()) : null;
        }
    }
}
