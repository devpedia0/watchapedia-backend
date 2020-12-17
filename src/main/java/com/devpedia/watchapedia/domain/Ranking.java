package com.devpedia.watchapedia.domain;
import com.devpedia.watchapedia.domain.enums.RankingChartIdState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "chartRank", "chartType", "chartId" }))
public class Ranking{
    @Id @GeneratedValue
    @Column(name = "ranking_id")
    private Long id;

    /**
     * 차트 랭킹 (1~30)
     */
    @Column(nullable = false)
    private Long chartRank;

    /**
     * 영화: movies, TV 프로그램: tvshows, 책: books
     */

    @Column(nullable = false)
    private String chartType;

    /**
     * box_office:박스오피스, mars:왓챠, netflix: 넷플릭스, predicted_rating: 별점, person:감독, person:배우, tag_match:태그, deck:컬렉션, deck_all: 모든컬렉션
     */
    @Column(nullable = false)
    private String chartId;


    /**
     * 콘텐츠 리스트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    public Ranking(Long chartRank, String chartType, String chartId, Content content){
        this.chartRank = chartRank;
        this.chartType = chartType;
        this.chartId = chartId;
        this.content = content;
    }

}
