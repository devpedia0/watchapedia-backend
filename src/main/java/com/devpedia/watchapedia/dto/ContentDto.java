package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.util.UrlUtil;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public class ContentDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommonContentInfo {

        private Long id;

        private String mainTitle;

        private String contentType;

        private LocalDate productionDate;

        private String posterImagePath;

        public CommonContentInfo(Content content) {
            this.id = content.getId();
            this.contentType = content.getDtype();
            this.mainTitle = content.getMainTitle();
            this.productionDate = content.getProductionDate();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath());
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommonContentRankingInfo {

        private Long id;

        private String mainTitle;

        private String contentType;

        private LocalDate productionDate;

        private String posterImagePath;

        private String chartId;

        private String chartType;

        private Long chartRank;

        public CommonContentRankingInfo(Content content, String chartId, String chartType, Long chartRank) {
            this.id = content.getId();
            this.contentType = content.getDtype();
            this.mainTitle = content.getMainTitle();
            this.productionDate = content.getProductionDate();
            this.posterImagePath = UrlUtil.getCloudFrontUrl(content.getPosterImage().getPath());
            this.chartId = chartId;
            this.chartType = chartType;
            this.chartRank = chartRank;
        }
    }

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
}
