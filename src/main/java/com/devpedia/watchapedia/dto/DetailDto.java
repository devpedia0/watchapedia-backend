package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.util.UrlUtil;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

public class DetailDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentDetail {
        private Object contentInfo;
        private ScoreAnalysis scores;
        private List<String> galleries;
        private List<ContentRole> participants;
        private CommentInfo comments;
        private CollectionInfo collections;
        private List<ContentDto.CollectionItem> similar;
        private UserContext context;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovieDetail {
        private String type;
        private String mainTitle;
        private String posterImagePath;
        private String category;
        private String description;
        private LocalDate productionDate;
        private String countryCode;
        private Boolean isWatchaContent;
        private Boolean isNetflixContent;
        private String originTitle;
        private Integer runningTime;

        public static MovieDetail of(Movie movie) {
            return MovieDetail.builder()
                    .type(movie.getDtype())
                    .mainTitle(movie.getMainTitle())
                    .posterImagePath(UrlUtil.getCloudFrontUrl(movie.getPosterImage().getPath()))
                    .category(movie.getCategory())
                    .description(movie.getDescription())
                    .productionDate(movie.getProductionDate())
                    .countryCode(movie.getCountryCode())
                    .isWatchaContent(movie.getIsWatchaContent())
                    .isNetflixContent(movie.getIsNetflixContent())
                    .originTitle(movie.getOriginTitle())
                    .runningTime(movie.getRunningTimeInMinutes())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookDetail {
        private String type;
        private String mainTitle;
        private String posterImagePath;
        private String category;
        private String description;
        private LocalDate productionDate;
        private String contents;
        private String elaboration;
        private Integer page;
        private String subtitle;
        private Long authorId;
        private String authorDescription;

        public static BookDetail of(Book book, Participant author) {
            return BookDetail.builder()
                    .type(book.getDtype())
                    .mainTitle(book.getMainTitle())
                    .posterImagePath(UrlUtil.getCloudFrontUrl(book.getPosterImage().getPath()))
                    .category(book.getCategory())
                    .description(book.getDescription())
                    .productionDate(book.getProductionDate())
                    .contents(book.getContents())
                    .elaboration(book.getElaboration())
                    .page(book.getPage())
                    .subtitle(book.getSubtitle())
                    .authorId(author != null ? author.getId() : null)
                    .authorDescription(author != null ? author.getDescription() : null)
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TvShowDetail {
        private String type;
        private String mainTitle;
        private String posterImagePath;
        private String category;
        private String description;
        private LocalDate productionDate;
        private String countryCode;
        private Boolean isWatchaContent;
        private Boolean isNetflixContent;
        private String originTitle;

        public static TvShowDetail of(TvShow tvShow) {
            return TvShowDetail.builder()
                    .type(tvShow.getDtype())
                    .mainTitle(tvShow.getMainTitle())
                    .posterImagePath(UrlUtil.getCloudFrontUrl(tvShow.getPosterImage().getPath()))
                    .category(tvShow.getCategory())
                    .description(tvShow.getDescription())
                    .productionDate(tvShow.getProductionDate())
                    .countryCode(tvShow.getCountryCode())
                    .isWatchaContent(tvShow.getIsWatchaContent())
                    .isNetflixContent(tvShow.getIsNetflixContent())
                    .originTitle(tvShow.getOriginTitle())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentRole {
        private Long id;
        private String name;
        private String profileImagePath;
        private String role;
        private String characterName;

        public static ContentRole of(ContentParticipant cp) {
            return ContentRole.builder()
                    .id(cp.getParticipant().getId())
                    .name(cp.getParticipant().getName())
                    .profileImagePath(
                            cp.getParticipant().getProfileImage() != null
                            ? UrlUtil.getCloudFrontUrl(cp.getParticipant().getProfileImage().getPath()) : null
                    )
                    .role(cp.getRole())
                    .characterName(cp.getCharacterName())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreAnalysis {
        private Integer totalCount;
        private Double average;
        private LinkedHashMap<String, Integer> distribution;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentInfo {
        private Integer count;
        private List<CommentDetail> list;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentDetail {
        private Long userId;
        private String userName;
        private String description;
        private Boolean isSpoiler;
        private Long replyCount;
        private Long likeCount;
        private InterestState interestState;
        private Double score;
        private Boolean isLiked;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectionInfo {
        private Integer count;
        private List<ContentDto.CollectionFourImages> list;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserContext {
        private Long userId;
        private InterestState interestState;
        private Double score;
        private String commentDescription;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentRequest {
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreRequest {
        private Double score;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InterestRequest {
        private InterestState state;
    }
}
