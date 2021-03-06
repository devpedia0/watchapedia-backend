package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.RankingChartIdState;
import com.devpedia.watchapedia.domain.enums.RankingChartTypeState;
import com.devpedia.watchapedia.domain.enums.RankingEnum;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.*;

import javax.persistence.Column;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RankingDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankingChartTypeRequest{
        @RankingEnum(enumClass = RankingChartTypeState.class, ignoreCase = true)
        private String chartType;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankingChartIdRequest{
        @RankingEnum(enumClass = RankingChartIdState.class, ignoreCase = true)
        private String chartId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankingContentInfo{
        private Long id;
        private Long chartRank;
        private String chartType;
        private Content content;
        public RankingContentInfo(Ranking ranking) {

            this.id = ranking.getId();
            this.chartRank = ranking.getChartRank();
            this.chartType = ranking.getChartType();
            this.content = ranking.getContent();
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankingContentChartMovieInfo {
        private Long id;

        private String mainTitle;

        private String contentType;

        private LocalDate productionDate;

        private String posterImagePath;

        private String chartId;

        private String chartType;

        private Long chartRank;

        private String originTitle;

        private String countryCode;

        private Integer runningTimeInMinutes;

        private Boolean isWatchaContent;

        private Boolean isNetflixContent;

        private Double bookRate;

        private Long totalAudience;

        public RankingContentChartMovieInfo(Content content, String chartId, String chartType, Long chartRank, Object originTitle, Object countryCode, Object runningTimeInMinutes, Object isWatchaContent, Object isNetflixContent, Object bookRate, Object totalAudience) {
            this.id = content.getId();
            this.contentType = content.getDtype();
            this.mainTitle = content.getMainTitle();
            this.productionDate = content.getProductionDate();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath());
            this.chartId = chartId;
            this.chartType = chartType;
            this.chartRank = chartRank;
            this.originTitle = (String) originTitle;
            this.countryCode = (String) countryCode;
            this.runningTimeInMinutes = (Integer) runningTimeInMinutes;
            this.isWatchaContent = (Boolean) isWatchaContent;
            this.isNetflixContent = (Boolean) isNetflixContent;
            this.bookRate = (Double) bookRate;
            this.totalAudience = (Long) totalAudience;
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankingContentChartTvShowInfo {
        private Long id;

        private String mainTitle;

        private String contentType;

        private LocalDate productionDate;

        private String posterImagePath;

        private String chartId;

        private String chartType;

        private Long chartRank;

        private String originTitle;

        private String countryCode;

        private Boolean isWatchaContent;

        private Boolean isNetflixContent;

        public RankingContentChartTvShowInfo(Content content, String chartId, String chartType, Long chartRank, Object originTitle, Object countryCode,Object isWatchaContent, Object isNetflixContent) {
            this.id = content.getId();
            this.contentType = content.getDtype();
            this.mainTitle = content.getMainTitle();
            this.productionDate = content.getProductionDate();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath());
            this.chartId = chartId;
            this.chartType = chartType;
            this.chartRank = chartRank;
            this.originTitle = (String) originTitle;
            this.countryCode = (String) countryCode;
            this.isWatchaContent = (Boolean) isWatchaContent;
            this.isNetflixContent = (Boolean) isNetflixContent;
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankingContentChartBookInfo {
        private Long id;

        private String mainTitle;

        private String contentType;

        private LocalDate productionDate;

        private String posterImagePath;

        private String chartId;

        private String chartType;

        private Long chartRank;

        private String contents;

        private String elaboration;

        private Integer page;

        private String subTitle;


        public RankingContentChartBookInfo(Content content, String chartId, String chartType, Long chartRank, Object contents, Object elaboration, Object page, Object subTitle) {
            this.id = content.getId();
            this.contentType = content.getDtype();
            this.mainTitle = content.getMainTitle();
            this.productionDate = content.getProductionDate();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath());
            this.chartId = chartId;
            this.chartType = chartType;
            this.chartRank = chartRank;
            this.contents = (String) contents;
            this.elaboration = (String) elaboration;
            this.page = (Integer) page;
            this.subTitle = (String) subTitle;

        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RankingContentInfoList {
        private String title;
        private String type;
        private List<Object> list;
    }
}
