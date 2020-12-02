package com.devpedia.watchapedia.dto;

import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.domain.Ranking;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

public class RankingDto {
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
        }
    }
}
