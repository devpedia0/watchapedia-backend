package com.devpedia.watchapedia.domain;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ranking {
    @Id @GeneratedValue
    @Column(name = "ranking_id")
    private Long id;

    /**
     * 차트 랭킹 (1~30)
     */
    @Column(nullable = false)
    private Long chart_rank;

    /**
     * 영화: M, TV 프로그램: T,  책: B
     */
    @Column(nullable = false)
    private String chart_type;

    /**
     * box_office:박스오피스, mars:왓챠, netflix: 넷플릭스, predicted_rating: 별점, person:감독, person:배우, tag_match:태그, deck:컬렉션, deck_all: 모든컬렉션
     */
    @Column(nullable = false)
    private String chart_id;

    /**
     * 콘텐츠 리스트
     */
    @OneToMany(mappedBy = "ranking", fetch = FetchType.LAZY)
    private List<Content> rankingContentList = new ArrayList<>();

    public Ranking(Long chart_rank, String chart_type, String chart_id, List<Content> rankingContentList){
        this.chart_rank = chart_rank;
        this.chart_type = chart_type;
        this.chart_id = chart_id;
        this.rankingContentList = rankingContentList;
    }


}
