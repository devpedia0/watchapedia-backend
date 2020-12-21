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
    private final String BOX_OFFICE_MOVIES= "box_office";
    private final String NETFLIX_MOVIES = "netflix";
    private final String MARS_MOVIES = "mars";
    private final String KOREA_TV_SHOWS = "korea_tv";
    private final String WATCHA_TV_SHOWS = "watcha_tv";
    private final String NETFLIX_TV_SHOWS = "netflix_tv";
    private final String ALL_BEST_SELLER_BOOKS = "all_best_seller";
    private final String NEW_BEST_SELLER_BOOKS = "new_best_seller";
    private final String MOST_SEARCHED_BOOKS = "most_searched";
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
        return setRankingList(rankings,chartType);
    }

    /**
     * 랭킹 차트 타입별 모든 데이터 조회
     * @param chartType 랭킹 차트 타입(movies, tv_shows, books)
     * @return 모든 랭킹 아이디 리스트
     */
    public List<RankingDto.RankingContentInfoList> searchWithAllRanking(String chartType) {
        List<Ranking> rankings = rankingRepository.findByChartAllId(chartType);
        return setRankingList(rankings,chartType);
    }

    /**
     * 랭킹 리스트 DTO 변환
     * @param rankings 랭킹 리스트
     * @return 랭킹 DTO 리스트 반환
     */
    private List<RankingDto.RankingContentInfoList> setRankingList(List<Ranking> rankings, String chartType){
        List<Object> rankingListContentsFirst = new ArrayList<>();
        List<Object> rankingListContentsSecond = new ArrayList<>();
        List<Object> rankingListContentsThird = new ArrayList<>();
        Object rankingInfo = null;
        Set<Long> ids = rankings.stream().map(ranking -> ranking.getContent().getId()).collect(Collectors.toSet());
        Map<Long, Double> rankingContentScore = rankingRepository.getRankingContentScore(ids);

        for(Ranking ranking : rankings){
            rankingInfo= getRankingContentChart(ranking,rankingContentScore);
            if(MOVIE.equals(chartType)){
                if(BOX_OFFICE_MOVIES.equals(ranking.getChartId())) rankingListContentsFirst.add(rankingInfo);
                else if(NETFLIX_MOVIES.equals(ranking.getChartId())) rankingListContentsSecond.add(rankingInfo);
                else if(MARS_MOVIES.equals(ranking.getChartId())) rankingListContentsThird.add(rankingInfo);
            }else if(TV_SHOWS.equals(chartType)){
                if(KOREA_TV_SHOWS.equals(ranking.getChartId())) rankingListContentsFirst.add(rankingInfo);
                else if(WATCHA_TV_SHOWS.equals(ranking.getChartId())) rankingListContentsSecond.add(rankingInfo);
                else if(NETFLIX_TV_SHOWS.equals(ranking.getChartId())) rankingListContentsThird.add(rankingInfo);
            }else if(BOOKS.equals(chartType)){
                if(ALL_BEST_SELLER_BOOKS.equals(ranking.getChartId())) rankingListContentsFirst.add(rankingInfo);
                else if(NEW_BEST_SELLER_BOOKS.equals(ranking.getChartId())) rankingListContentsSecond.add(rankingInfo);
                else if(MOST_SEARCHED_BOOKS.equals(ranking.getChartId())) rankingListContentsThird.add(rankingInfo);
            }
        }
        return getChartInfoList(chartType, rankingListContentsFirst,rankingListContentsSecond,rankingListContentsThird);
    }

    /**
     * 랭킹 컨텐츠 DTO 리스트 반환
     * @param chartType
     * @param rankingListContentsFirst 랭킹 첫번째 컨텐츠 리스트
     * @param rankingListContentsSecond 랭킹 두번째 컨텐츠 리스트
     * @param rankingListContentsThird 랭킹 세번째 컨텐츠 리스트
     * @return랭킹 차트 타입(movies, tv_shows, books)
     */

    private List<RankingDto.RankingContentInfoList> getChartInfoList(String chartType, List<Object> rankingListContentsFirst, List<Object> rankingListContentsSecond, List<Object> rankingListContentsThird) {
        String[] titleList = getChartTitle(chartType);
        List<RankingDto.RankingContentInfoList> resultRankingInfoList = new ArrayList<>();
        if(!rankingListContentsFirst.isEmpty()) {
            if (MOVIE.equals(chartType)) resultRankingInfoList.add(new RankingDto.RankingContentInfoList(titleList[0], BOX_OFFICE_MOVIES, rankingListContentsFirst.subList(0, rankingListContentsFirst.size() > 30 ? 30 : rankingListContentsFirst.size())));
            else if (TV_SHOWS.equals(chartType)) resultRankingInfoList.add(new RankingDto.RankingContentInfoList(titleList[0], KOREA_TV_SHOWS, rankingListContentsFirst.subList(0, rankingListContentsFirst.size() > 30 ? 30 : rankingListContentsFirst.size())));
            else if (BOOKS.equals(chartType)) resultRankingInfoList.add(new RankingDto.RankingContentInfoList(titleList[0], ALL_BEST_SELLER_BOOKS, rankingListContentsFirst.subList(0, rankingListContentsFirst.size() > 30 ? 30 : rankingListContentsFirst.size())));
        }
        if(!rankingListContentsSecond.isEmpty()) {
            if (MOVIE.equals(chartType)) resultRankingInfoList.add(new RankingDto.RankingContentInfoList(titleList[1], MARS_MOVIES, rankingListContentsSecond.subList(0, rankingListContentsSecond.size() > 30 ? 30 : rankingListContentsSecond.size())));
            else if (TV_SHOWS.equals(chartType)) resultRankingInfoList.add(new RankingDto.RankingContentInfoList(titleList[1], WATCHA_TV_SHOWS, rankingListContentsSecond.subList(0, rankingListContentsSecond.size() > 30 ? 30 : rankingListContentsSecond.size())));
            else if (BOOKS.equals(chartType)) resultRankingInfoList.add(new RankingDto.RankingContentInfoList(titleList[1], NEW_BEST_SELLER_BOOKS, rankingListContentsSecond.subList(0, rankingListContentsSecond.size() > 30 ? 30 : rankingListContentsSecond.size())));
        }
        if(!rankingListContentsThird.isEmpty()) {
            if (MOVIE.equals(chartType)) resultRankingInfoList.add(new RankingDto.RankingContentInfoList(titleList[2], NETFLIX_MOVIES, rankingListContentsThird.subList(0, rankingListContentsThird.size() > 30 ? 30 : rankingListContentsThird.size())));
            else if (TV_SHOWS.equals(chartType)) resultRankingInfoList.add(new RankingDto.RankingContentInfoList(titleList[2], NETFLIX_TV_SHOWS, rankingListContentsThird.subList(0, rankingListContentsThird.size() > 30 ? 30 : rankingListContentsThird.size())));
            else if (BOOKS.equals(chartType)) resultRankingInfoList.add(new RankingDto.RankingContentInfoList(titleList[2], MOST_SEARCHED_BOOKS, rankingListContentsThird.subList(0, rankingListContentsThird.size() > 30 ? 30 : rankingListContentsThird.size())));
        }
        return resultRankingInfoList;
    }

    /**
     * 차트 타입별 랭킹 타이틀 리스 값 반환
     * @param chartType
     * @return
     */

    private String[] getChartTitle(String chartType) {
        String[] titleList= new String[3];
        if(MOVIE.equals(chartType)){
            titleList[0] = "박스오피스";
            titleList[1] = "왓챠 영화 순위";
            titleList[2] = "넷플릭스 영화 순위";
        }
        else if(TV_SHOWS.equals(chartType)){
            titleList[0] = "한국 TV 프로그램 인기 순위";
            titleList[1] = "왓챠 TV 프로그램 순위";
            titleList[2] = "넷플릭스 TV 프로그램 순위";
        }
        else if(BOOKS.equals(chartType)){
            titleList[0] = "전체 베스트셀러";
            titleList[1] = "신간 베스트 셀러";
            titleList[2] = "가장 많이 검색된 책";
        }
        return titleList;
    }

    /**
     * 랭킹 컨텐츠별 DTO 세팅 및 반환
     * @param ranking 조회해온 랭킹 리스트
     * @param rankingContentScore 평점 리스
     * @return
     */

    private Object getRankingContentChart(Ranking ranking, Map<Long, Double> rankingContentScore) {
        RankingDto.RankingContentChartMovieInfo rankingContentChartMovieInfo = null;
        RankingDto.RankingContentChartTvShowInfo rankingContentChartTvShowInfo = null;
        RankingDto.RankingContentChartBookInfo rankingContentChartBookInfo = null;
        Map<String,Object> hm = getProxyValueList(ranking);

        if(MOVIE.equals(ranking.getChartType())){
            if (rankingContentScore.containsKey(ranking.getContent().getId())) {
                hm.put("bookRate", rankingContentScore.get(ranking.getContent().getId()));
            }
            rankingContentChartMovieInfo = new RankingDto.RankingContentChartMovieInfo(
                    ranking.getContent()
                    , ranking.getChartId()
                    , ranking.getChartType()
                    , ranking.getChartRank()
                    , hm.get("originTitle")
                    , hm.get("countryCode")
                    , hm.get("runningTimeInMinutes")
                    , hm.get("isWatchaContent")
                    , hm.get("isNetflixContent")
                    , hm.get("bookRate")
                    , hm.get("totalAudience")
            );
        } else if (TV_SHOWS.equals(ranking.getChartType())) {
            rankingContentChartTvShowInfo = new RankingDto.RankingContentChartTvShowInfo(
                    ranking.getContent()
                    , ranking.getChartId()
                    , ranking.getChartType()
                    , ranking.getChartRank()
                    , hm.get("originTitle")
                    , hm.get("countryCode")
                    , hm.get("isWatchaContent")
                    , hm.get("isNetflixContent")
            );
        } else if (BOOKS.equals(ranking.getChartType())) {
            rankingContentChartBookInfo = new RankingDto.RankingContentChartBookInfo(
                    ranking.getContent()
                    , ranking.getChartId()
                    , ranking.getChartType()
                    , ranking.getChartRank()
                    , hm.get("contents")
                    , hm.get("subtitle")
                    , hm.get("page")
                    , hm.get("elaboration")
            );
        }
        if(rankingContentChartMovieInfo != null){
            return rankingContentChartMovieInfo;
        }else if(rankingContentChartTvShowInfo != null){
            return rankingContentChartTvShowInfo;
        }else {
            return rankingContentChartBookInfo;
        }
    }

    /**
     * 프록시 객체 사용을 위한 변환
     * @param ranking 조회해온 랭킹 리스트
     * @return
     */
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
