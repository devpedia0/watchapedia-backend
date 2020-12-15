package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

public class ContentDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContentChildren {
        private List<ParticipantDto.ParticipantRole> roles;
        private List<Long> tags;
        private List<MultipartFile> gallery;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MainListItem {
        private Long id;
        private String posterImagePath;
        private String mainTitle;
        private Boolean isWatchaContent;
        private Boolean isNetflixContent;
        private Double score;

        public MainListItem(Movie movie, Double score) {
            this.id = movie.getId();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(movie.getPosterImage().getPath());
            this.mainTitle = movie.getMainTitle();
            this.isWatchaContent = movie.getIsWatchaContent();
            this.isNetflixContent = movie.getIsNetflixContent();
            this.score = score;
        }

        public MainListItem(TvShow tvShow, Double score) {
            this.id = tvShow.getId();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(tvShow.getPosterImage().getPath());
            this.mainTitle = tvShow.getMainTitle();
            this.isWatchaContent = tvShow.getIsWatchaContent();
            this.isNetflixContent = tvShow.getIsNetflixContent();
            this.score = score;
        }

        public MainListItem(Book book, Double score) {
            this.id = book.getId();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(book.getPosterImage().getPath());
            this.mainTitle = book.getMainTitle();
            this.isWatchaContent = null;
            this.isNetflixContent = null;
            this.score = score;
        }

        public static <T extends Content> MainListItem of(T content, Double score) {
            if (content instanceof Movie) return new MainListItem((Movie) content, score);
            else if (content instanceof Book) return new MainListItem((Book) content, score);
            else return new MainListItem((TvShow) content, score);
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MainList {
        private String type;
        private String title;
        private List<MainListItem> list;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MainListForCollection {
        private Long id;
        private String type;
        private String title;
        private String subtitle;
        private List<MainListItem> list;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AwardItem {
        private Long id;
        private String title;
        private List<String> images;

        public <T extends Content> AwardItem(Collection collection, List<T> contents) {
            this.id = collection.getId();
            this.title = collection.getTitle();
            images = new ArrayList<>();
            for (Content content : contents) {
                images.add(UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath()));
            }
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListForAward {
        private String type;
        private String title;
        private List<AwardItem> list;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectionDetail {
        private String userName;
        private String title;
        private String description;
        private Integer contentCount;
        private List<CollectionItem> list;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectionItem {
        private Long id;
        private String posterImagePath;
        private String mainTitle;
        private Boolean isWatchaContent;
        private Boolean isNetflixContent;
        private Double score;
        private String type;

        public CollectionItem(Movie movie, Double score) {
            this.id = movie.getId();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(movie.getPosterImage().getPath());
            this.mainTitle = movie.getMainTitle();
            this.isWatchaContent = movie.getIsWatchaContent();
            this.isNetflixContent = movie.getIsNetflixContent();
            this.score = score;
            this.type = "M";
        }

        public CollectionItem(TvShow tvShow, Double score) {
            this.id = tvShow.getId();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(tvShow.getPosterImage().getPath());
            this.mainTitle = tvShow.getMainTitle();
            this.isWatchaContent = tvShow.getIsWatchaContent();
            this.isNetflixContent = tvShow.getIsNetflixContent();
            this.score = score;
            this.type = "S";
        }

        public CollectionItem(Book book, Double score) {
            this.id = book.getId();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(book.getPosterImage().getPath());
            this.mainTitle = book.getMainTitle();
            this.isWatchaContent = null;
            this.isNetflixContent = null;
            this.score = score;
            this.type = "B";
        }

        public static <T extends Content> CollectionItem of(T content, Double score) {
            if (content instanceof Movie) return new CollectionItem((Movie) content, score);
            else if (content instanceof Book) return new CollectionItem((Book) content, score);
            else return new CollectionItem((TvShow) content, score);
        }
    }

}
