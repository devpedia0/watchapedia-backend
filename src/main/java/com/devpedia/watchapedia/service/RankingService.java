package com.devpedia.watchapedia.service;
import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.dto.RankingDto;
import com.devpedia.watchapedia.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.lang.reflect.Field;
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
    private final String MOVIE = "movies";
    private final String TV_SHOWS = "tv_shows";
    private final String BOOKS = "books";

    /**
     * 랭킹 차트 아이디 데이터 개별 조회
     * @param chartType 랭킹 차트 타입(movies, tv_shows, books)
     * @param chartId 랭킹 차트 아이디(box_office,netflix,mars)
     * @return 랭킹 아이디 리스트
     */
    public List<RankingDto.RankingContentInfoList> searchWithRanking(String chartType, String chartId) {
        List<Ranking> rankings = rankingRepository.findByChartId(chartType, chartId);
        return setRankingList(rankings);
    }

    /**
     * 랭킹 차트 타입별 모든 데이터 조회
     * @param chartType 랭킹 차트 타입(movies, tv_shows, books)
     * @return 모든 랭킹 아이디 리스트
     */
    public List<RankingDto.RankingContentInfoList> searchWithAllRanking(String chartType) {
        List<Ranking> rankings = rankingRepository.findByChartAllId(chartType);
        return setRankingList(rankings);
    }

    /**
     * 랭킹 리스트 DTO 변환 함수
     * @param rankings 랭킹 리스트
     * @return 랭킹 DTO 리스트 반환
     */

    private List<RankingDto.RankingContentInfoList> setRankingList(List<Ranking> rankings){
        List<RankingDto.RankingContentInfoList> resultRankingInfoList = new ArrayList<>();
        List<RankingDto.RankingContentChartInfo> boxOfficeList = new ArrayList<>();
        List<RankingDto.RankingContentChartInfo> netflixList = new ArrayList<>();
        List<RankingDto.RankingContentChartInfo> marsList = new ArrayList<>();
        RankingDto.RankingContentChartInfo rankingInfo = new RankingDto.RankingContentChartInfo();
        Set<Long> ids = rankings.stream().map(ranking -> ranking.getContent().getId()).collect(Collectors.toSet());
        Map<Long, Double> rankingContentScore = rankingRepository.getRankingContentScore(ids);

        for(Ranking ranking : rankings){
            Map<String,Object> hs = getProxyValueList(ranking);
            if (rankingContentScore.containsKey(ranking.getContent().getId())) {
                hs.put("bookRate", rankingContentScore.get(ranking.getContent().getId()));
            }
            rankingInfo = new RankingDto.RankingContentChartInfo(
                    ranking.getContent()
                    ,ranking.getChartId()
                    ,ranking.getChartType()
                    ,ranking.getChartRank()
                    ,hs.get("originTitle")
                    ,hs.get("countryCode")
                    ,hs.get("runningTimeInMinutes")
                    ,hs.get("isWatchaContent")
                    ,hs.get("isNetflixContent")
                    ,hs.get("bookRate")
                    ,hs.get("totalAudience")
            );
            if(BOX_OFFICE.equals(ranking.getChartId())) {
                boxOfficeList.add(rankingInfo);
            }
            if(NETFLIX.equals(ranking.getChartId())) {
                netflixList.add(rankingInfo);
            }
            if(MARS.equals(ranking.getChartId())) {
                marsList.add(rankingInfo);
            }
        }
        if(!boxOfficeList.isEmpty()){
            resultRankingInfoList.add(new RankingDto.RankingContentInfoList("박스오피스", BOX_OFFICE,boxOfficeList.subList(0,boxOfficeList.size() > 30 ? 30 : boxOfficeList.size())));
        }
        if(!netflixList.isEmpty()){
            resultRankingInfoList.add(new RankingDto.RankingContentInfoList("넷플릭스 영화 순위",NETFLIX,netflixList.subList(0,netflixList.size() > 10 ? 10 : netflixList.size())));
        }
        if(!marsList.isEmpty()){
            resultRankingInfoList.add(new RankingDto.RankingContentInfoList("왓챠 영화 순위",MARS,marsList.subList(0,marsList.size() > 10 ? 10 : marsList.size())));
        }
        return resultRankingInfoList;
    }

    private Map<String, Object> getProxyValueList(Ranking ranking) {
        Object proxyObj = Hibernate.unproxy(ranking.getContent());
        Map<String, Object> hm = new HashMap<>();

        for(Field field : proxyObj.getClass().getDeclaredFields()){
            field.setAccessible(true);
            try {
                hm.put(field.getName(), field.get(proxyObj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return hm;
    }

}
