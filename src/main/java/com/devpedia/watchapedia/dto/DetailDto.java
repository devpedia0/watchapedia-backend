package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DetailDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class addDetailInfo {
        private Long id;

        private String mainTitle;

        private String contentType;

        private LocalDate productionDate;

        private String posterImagePath;

        private String category;

        private String description;

        private String originTitle;

        private String countryCode;

        private Boolean isWatchaContent;

        private Boolean isNetflixContent;

        private List<ParticipantDto.ParticipantInfo> participants;
        private List<ImageDto.ImageInfo> gallary;
        private List<TagDto.TagInfo> tags;

        private Double avgScore;
        private Integer scoreCount;
        private Map<Double,Integer> detailScoreCountList;
        private ContentDto.ListForAward awardList;
        private List<Object> similarContentsList;
        private List<DetailDto.addDetailCommentInfoList> detailCommentInfoList;
        private DetailDto.DetailUserCommentInfoList detailUserCommentInfoList;
        public addDetailInfo(Content content, DetailRankingScoreInfoList DetailRankingScoreInfoList, Map<Double, Integer> detailScoreCountList, String category, String description, String originTitle, String countryCode, Boolean isNetflixContent, Boolean isWatchaContent, List<addDetailCommentInfoList> detailCommentInfoList, List<ParticipantDto.ParticipantInfo> participants, List<ImageDto.ImageInfo> gallary, List<TagDto.TagInfo> tagList, ContentDto.ListForAward awardList, List<Object> similarContentsList, DetailUserCommentInfoList detailUserCommentInfoList, Long allCommentCount) {
            this.id = content.getId();
            this.contentType = content.getDtype();
            this.mainTitle = content.getMainTitle();
            this.productionDate = content.getProductionDate();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath());
            this.avgScore = DetailRankingScoreInfoList.getAvgScore();
            this.scoreCount = DetailRankingScoreInfoList.getScoreCount();
            this.detailScoreCountList = detailScoreCountList;
            this.detailCommentInfoList = detailCommentInfoList;
            this.category = category;
            this.description = description;
            this.originTitle = originTitle;
            this.countryCode = countryCode;
            this.isWatchaContent = isWatchaContent;
            this.isNetflixContent = isNetflixContent;
            this.participants = participants;
            this.gallary = gallary;
            this.tags= tagList;
            this.awardList = awardList;
            this.similarContentsList = similarContentsList;
            this.detailUserCommentInfoList = detailUserCommentInfoList;

        }

        public addDetailInfo(Content content, DetailRankingScoreInfoList DetailRankingScoreInfoList, Map<Double, Integer> detailScoreCountList, String category, LocalDate productionDate, String description, List<addDetailCommentInfoList> detailCommentInfoList, List<ParticipantDto.ParticipantInfo> contentParticipant, List<ImageDto.ImageInfo> gallary, List<TagDto.TagInfo> tagList, ContentDto.ListForAward awardList, List<Object> similarContentsList, DetailDto.DetailUserCommentInfoList detailUserCommentInfoList, Long allCommentCount) {
            this.id = content.getId();
            this.contentType = content.getDtype();
            this.mainTitle = content.getMainTitle();
            this.productionDate = content.getProductionDate();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath());
            this.avgScore = DetailRankingScoreInfoList.getAvgScore();
            this.scoreCount = DetailRankingScoreInfoList.getScoreCount();
            this.detailScoreCountList = detailScoreCountList;
            this.detailCommentInfoList = detailCommentInfoList;
            this.category = category;
            this.description = description;
            this.participants = contentParticipant;
            this.gallary = gallary;
            this.tags = tagList;
            this.awardList = awardList;
            this.similarContentsList = similarContentsList;
            this.detailUserCommentInfoList = detailUserCommentInfoList;

        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class addDetailSimilarInfo {
        private Long id;

        private String mainTitle;

        private String contentType;

        private LocalDate productionDate;

        private String posterImagePath;

        private String category;

        private String description;

        private String originTitle;

        private String countryCode;

        private Boolean isWatchaContent;

        private Boolean isNetflixContent;
        private Double avgScore;
        private Integer scoreCount;
        Map<Double,Integer> detailUserScoreCountList;

        public addDetailSimilarInfo(Content content, DetailRankingScoreInfoList DetailRankingScoreInfoList, Map<Double, Integer> detailUserScoreCountList, String category, String description, String originTitle, String countryCode, Boolean isWatchaContent, Boolean isNetflixContent) {
            this.id = content.getId();
            this.contentType = content.getDtype();
            this.mainTitle = content.getMainTitle();
            this.productionDate = content.getProductionDate();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath());
            this.avgScore = DetailRankingScoreInfoList.getAvgScore();
            this.scoreCount = DetailRankingScoreInfoList.getScoreCount();
            this.detailUserScoreCountList = detailUserScoreCountList;
            this.category = category;
            this.description = description;
            this.originTitle = originTitle;
            this.countryCode = countryCode;
            this.isWatchaContent = isWatchaContent;
            this.isNetflixContent = isNetflixContent;
        }

        public addDetailSimilarInfo(Content content, DetailRankingScoreInfoList DetailRankingScoreInfoList, Map<Double, Integer> detailUserScoreCountList, String category, LocalDate productionDate, String description) {
            this.id = content.getId();
            this.contentType = content.getDtype();
            this.mainTitle = content.getMainTitle();
            this.productionDate = content.getProductionDate();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath());
            this.avgScore = DetailRankingScoreInfoList.getAvgScore();
            this.scoreCount = DetailRankingScoreInfoList.getScoreCount();
            this.detailUserScoreCountList = detailUserScoreCountList;
            this.category = category;
            this.description = description;
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailContentInfoList {
        private String title;
        private String type;
        private Object list;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailRankingScoreInfoList {
        private Double avgScore;
        private Integer scoreCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class addDetailCommentInfoList {
        private String description;
        private Boolean containsSpoiler;
        private String name;
        private Long userLikeCount;
        private Double userScore;
        private Long userReplyCount;
        private String interestState;
        private Boolean userLikeState;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailUserCommentInfoList {
        private Long contentIdObj;
        private Long userIdObj;
        private String description;
        private Double scoreAvg;
        private String interestState;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class addDetailParticipantInfoList {
        private Long id;

        private String mainTitle;

        private String contentType;

        private LocalDate productionDate;

        private String posterImagePath;

        private Double scoreAvg;

        public addDetailParticipantInfoList(Content content, Double scoreAvg) {
            this.id = content.getId();
            this.contentType = content.getDtype();
            this.mainTitle = content.getMainTitle();
            this.productionDate = content.getProductionDate();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath());
            this.scoreAvg = scoreAvg;
        }
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class detailCommentLikeInfo {
        private Long likeUserId;
        private Long contentId;
        private Long commentUserId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class detailCommentInsertInfo {
        private Long contentId;
        private Long userId;
        private Long commentUserId;
        private String description;
        private Boolean containsSpoiler;
        private Boolean isDeleted;
        private Long userCount;
        private Long state;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class detailCommentDeleteInfo {
        private Long contentId;
        private Long userId;
        private Long state;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class detailCommentUpdateInfo {
        private Long contentId;
        private Long userId;
        private Long state;
        private String description;

    }
}
