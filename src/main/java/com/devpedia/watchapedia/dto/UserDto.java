package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.domain.enums.AccessRange;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.dto.enums.InterestContentOrder;
import com.devpedia.watchapedia.dto.enums.RatingContentOrder;
import lombok.*;

import javax.validation.constraints.*;
import java.util.LinkedHashMap;
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
    public static class UserActionCounts {
        private ActionCounts movie;
        private ActionCounts book;
        private ActionCounts tvShow;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActionCounts {
        private Integer ratingCount;
        private Integer wishCount;
        private Integer watchingCount;
        private Integer notInterestCount;
        private Integer commentCount;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RatingContentParameter {
        private ContentTypeParameter type;
        private RatingContentOrder order;
        private Integer page;
        private Integer size;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRatingGroup {
        private Integer count;
        private List<ContentDto.MainListItem> list;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InterestContentParameter {
        private ContentTypeParameter type;
        private InterestState state;
        private InterestContentOrder order;
        private Integer page;
        private Integer size;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserAnalysisData {
        private String userName;
        private UserRatingAnalysis rating;
        private UserMovieAnalysis movie;
        private UserBookAnalysis book;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRatingAnalysis {
        private Integer movieCount;
        private Integer bookCount;
        private Integer tvShowCount;
        private int totalCount;
        private Double average;
        private Double mostRating;
        private LinkedHashMap<Double, Integer> distribution;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserMovieAnalysis {
        private List<FavoriteCommon> tag;
        private List<FavoriteCommon> country;
        private List<FavoriteCommon> category;
        private List<FavoritePerson> actor;
        private List<FavoritePerson> director;
        private Integer totalRunningTimeInMinute;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserBookAnalysis {
        private List<FavoriteCommon> tag;
        private List<FavoritePerson> author;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FavoriteCommon {
        private String description;
        private Double score;
        private Integer count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FavoritePerson {
        private Long id;
        private String name;
        private String profileImagePath;
        private String movieName;
        private Double score;
        private Integer count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchUserItem {
        private Long id;
        private String name;
        private String description;
        private ActionCounts counts;
    }
}
