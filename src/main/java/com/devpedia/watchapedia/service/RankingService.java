package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Ranking;
import com.devpedia.watchapedia.dto.RankingDto;
import com.devpedia.watchapedia.repository.ContentRepository;
import com.devpedia.watchapedia.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RankingService {
    private final RankingRepository rankingRepository;
    private final ContentRepository contentRepository;
    public List<RankingDto.RankingContentInfo> searchWithRanking() {
        List<RankingDto.RankingContentInfo> res = new ArrayList<>();
        return res;
    }

}
