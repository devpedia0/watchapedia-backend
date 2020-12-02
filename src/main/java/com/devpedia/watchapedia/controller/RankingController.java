package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.RankingDto;
import com.devpedia.watchapedia.service.RankingService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RankingController {

    private final RankingService rankingService;

    /**
     * 랭킹 차트 아이디 데이터 개별 조회
     * @param chartType
     * @param chartId
     * @return
     */
    @GetMapping("/public/{chartType}/rankings")
    public List<ContentDto.CommonContentRankingInfo> getRankingList(@PathVariable("chartType") String chartType
            , @RequestParam("chartId") String chartId){
        return rankingService.searchWithRanking(chartType, chartId);
    }

    /**
     * 랭킹 차트 타입별 모든 데이터 조회
     * @param chartType
     * @return
     */
    @GetMapping("/public/{chartType}/rankings/contents")
    public HashMap<String,List<ContentDto.CommonContentRankingInfo>> getRankingAllList(@PathVariable("chartType") String chartType){
        return rankingService.searchWithAllRanking(chartType);

    }
}
