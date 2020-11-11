package com.devpedia.watchapedia.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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
}
