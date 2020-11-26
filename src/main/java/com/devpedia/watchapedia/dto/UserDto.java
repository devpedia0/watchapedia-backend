package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.domain.enums.AccessRange;
import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.*;
import java.util.List;

public class UserDto {
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignupRequest {
        @Email
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String name;
        @NotBlank
        @Pattern(regexp = "^[A-Z]{2}$", message = "국가코드는 2자리 영어 대문자입니다")
        private String countryCode;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SigninRequest {
        @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OAuthTokenInfo {
        @NotBlank
        private String accessToken;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacebookUserInfo {
        @NotBlank
        private String email;
        @NotBlank
        private String name;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmailCheckResult {
        @NotBlank
        private boolean isExist;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        @NotBlank
        private String name;
        @NotBlank
        private String email;

        private String description;
        @NotBlank
        private String countryCode;
        @NotBlank
        private AccessRange accessRange;
        @NotNull
        private Boolean isEmailAgreed;
        @NotNull
        private Boolean isSmsAgreed;
        @NotNull
        private Boolean isPushAgreed;
        @NotNull
        private List<String> roles;

        public UserInfo(User user) {
            this.name = user.getName();
            this.email = user.getEmail();
            this.countryCode = user.getCountryCode();
            this.description = user.getDescription();
            this.isEmailAgreed = user.getIsEmailAgreed();
            this.isSmsAgreed = user.getIsSmsAgreed();
            this.isPushAgreed = user.getIsPushAgreed();
            this.accessRange = user.getAccessRange();
            this.roles = user.getRoles();
        }
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoMinimum {
        private Long id;

        private String name;

        private String email;

        private List<String> roles;

        public UserInfoMinimum(User user) {
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.roles = user.getRoles();
        }
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoEditRequest {
        private String name;

        private String description;
        @Pattern(regexp = "^[A-Z]{2}$", message = "국가코드는 2자리 영어 대문자입니다")
        private String countryCode;

        private AccessRange accessRange;

        private Boolean isEmailAgreed;

        private Boolean isSmsAgreed;

        private Boolean isPushAgreed;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectionInsertRequest {
        @NotBlank
        private String title;
        @NotBlank
        private String description;

        private List<Long> contents;
    }
}
