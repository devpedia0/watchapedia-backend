package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.RankingDto;
import com.devpedia.watchapedia.service.RankingService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RankingController {

    private final RankingService rankingService;

    /**
     * 랭킹 차트 아이디 데이터 개별 조회
     * @param chartType 차트 타입(영화,티비쇼,책)
     * @param chartId 차트 아이디(박스오피스,넷플릭스,왓챠)
     * @return 랭킹 차트 DTO 리스트 반환
     */
    @GetMapping("/public/ranking/{chartType}")
    public List<RankingDto.RankingContentInfoList> getRankingList(@PathVariable("chartType") String chartType
            , @RequestParam("chartId") String chartId){
        return rankingService.searchWithRanking(chartType, chartId);
    }

    /**
     * 랭킹 차트 타입별 모든 데이터 조회
     * @param chartType 차트 타입(영화,티비쇼,책)
     * @return 랭킹 차트 DTO 리스트 반환
     */
    @GetMapping("/public/rankings/{chartType}")
    public List<RankingDto.RankingContentInfoList> getRankingAllList(@PathVariable("chartType") String chartType){
        return rankingService.searchWithAllRanking(chartType);
    }

}
