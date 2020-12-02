package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.domain.Ranking;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.RankingDto;
import com.devpedia.watchapedia.repository.ContentRepository;
import com.devpedia.watchapedia.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RankingService {
    private final RankingRepository rankingRepository;
    private final String BOX_OFFICE = "box_office";
    private final String NETFLIX = "netflix";
    private final String MARS = "mars";
    private final String PREDICTED_RATING = "predicted_rating";
    private final String PERSON = "person";
    private final String TAG_MATCH = "tag_match";
    private final String DECK = "deck";
    private final String DECK_ALL = "deck_all";

    /**
     * 랭킹 차트 아이디 데이터 개별 조회
     * @param chartType
     * @param chartId
     * @return
     */
    public List<ContentDto.CommonContentRankingInfo> searchWithRanking(String chartType, String chartId) {
        List<Ranking> rankings = rankingRepository.findByChartId(chartType, chartId);
        List<ContentDto.CommonContentRankingInfo> resultMap = new ArrayList<>();
        ContentDto.CommonContentRankingInfo contentRankingInfo;

        for(Ranking ranking : rankings){
            contentRankingInfo = new ContentDto.CommonContentRankingInfo(rankingRepository.findByRankingId(ranking.getContent().getId())
                    , ranking.getChartId()
                    , ranking.getChartType()
                    , ranking.getChartRank()
            );
            resultMap.add(contentRankingInfo);
        }
        return resultMap;
    }

    /**
     * 랭킹 차트 타입별 모든 데이터 조회
     * @param chartType
     * @return
     */
    public HashMap<String,List<ContentDto.CommonContentRankingInfo>> searchWithAllRanking(String chartType) {
        List<Ranking> rankings = rankingRepository.findByChartAllId(chartType);
        HashMap<String,List<ContentDto.CommonContentRankingInfo>> resultMap = new HashMap<>();
        ContentDto.CommonContentRankingInfo contentRankingInfo;
        List<ContentDto.CommonContentRankingInfo> boxOfficeList = new ArrayList<>();
        List<ContentDto.CommonContentRankingInfo> netflixList = new ArrayList<>();
        List<ContentDto.CommonContentRankingInfo> marsList = new ArrayList<>();
        List<ContentDto.CommonContentRankingInfo> predictedList = new ArrayList<>();
        List<ContentDto.CommonContentRankingInfo> personList = new ArrayList<>();
        List<ContentDto.CommonContentRankingInfo> tagList = new ArrayList<>();
        List<ContentDto.CommonContentRankingInfo> deckList = new ArrayList<>();
        List<ContentDto.CommonContentRankingInfo> deckAllList = new ArrayList<>();

        for(Ranking ranking : rankings){
            contentRankingInfo = new ContentDto.CommonContentRankingInfo(rankingRepository.findByRankingId(ranking.getContent().getId())
                    , ranking.getChartId()
                    , ranking.getChartType()
                    , ranking.getChartRank()
            );
            if(BOX_OFFICE.equals(ranking.getChartId()))
                boxOfficeList.add(contentRankingInfo);
            else if(NETFLIX.equals(ranking.getChartId()))
                netflixList.add(contentRankingInfo);
            else if(MARS.equals(ranking.getChartId()))
                marsList.add(contentRankingInfo);
            else if(PREDICTED_RATING.equals(ranking.getChartId()))
                predictedList.add(contentRankingInfo);
            else if(PERSON.equals(ranking.getChartId()))
                personList.add(contentRankingInfo);
            else if(TAG_MATCH.equals(ranking.getChartId()))
                tagList.add(contentRankingInfo);
            else if(DECK.equals(ranking.getChartId()))
                deckList.add(contentRankingInfo);
            else if(DECK_ALL.equals(ranking.getChartId()))
                deckAllList.add(contentRankingInfo);
        }
        resultMap.put(BOX_OFFICE,boxOfficeList);
        resultMap.put(NETFLIX,netflixList);
        resultMap.put(MARS, marsList);
        resultMap.put(PERSON, personList);
        resultMap.put(TAG_MATCH, tagList);
        resultMap.put(DECK, deckList);
        resultMap.put(DECK_ALL, deckAllList);
        return resultMap;
    }
}
