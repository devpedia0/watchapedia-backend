package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Ranking;
import com.devpedia.watchapedia.dto.RankingDto;
import com.devpedia.watchapedia.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RankingService {
    private final RankingRepository rankingRepository;

    public List<RankingDto.RankingContentInfo> searchWithRanking(String chart_type, String chart_id) {
        List<Ranking> rankingList = rankingRepository.findChartId(chart_type, chart_id);
        return rankingList.stream()
                .map(RankingDto.RankingContentInfo::new)
                .collect(Collectors.toList());
    }
}
