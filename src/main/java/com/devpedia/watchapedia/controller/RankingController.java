package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.RankingDto;
import com.devpedia.watchapedia.service.RankingService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/public/{charType}/rankings")
    public List<RankingDto.RankingContentInfo> getRankingList(@PathVariable("charType") String chartType
            , @RequestParam("charId") String chartId){
        List<RankingDto.RankingContentInfo> result = rankingService.searchWithRanking(chartType, chartId);
        return result;
    }


}
