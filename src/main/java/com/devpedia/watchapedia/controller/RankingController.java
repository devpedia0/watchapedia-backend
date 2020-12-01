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

    @GetMapping("/api/home/{chart_type}/rankings")
    public List<RankingDto.RankingContentInfo> getRankingList(@PathVariable("chart_type") String chart_type
            , @RequestParam("chart_id") String chart_id){
        List<RankingDto.RankingContentInfo> result = rankingService.searchWithRanking(chart_type, chart_id);
        return result;
    }


}
