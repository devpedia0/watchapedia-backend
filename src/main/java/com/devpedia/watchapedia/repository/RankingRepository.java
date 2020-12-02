package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.domain.Ranking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RankingRepository {
    private final EntityManager em;

    /**
     * 컨텐츠 테이블 랭킹 아이디 값 조회
     * @param rankingIds
     * @return
     */
    public Content findByRankingId(Long rankingId){
        String jpqlQuery = "select c " +
                "from Content c " +
                "where c.id = :rankingId";
        return em.createQuery(jpqlQuery, Content.class)
                .setParameter("rankingId",rankingId)
                .getSingleResult();
    }

    /**
     * 랭킹 차트 아이디 & 차트 타입 조회
     * @param chartType
     * @param chartId
     * @return
     */
    public List<Ranking> findByChartId(String chartType, String chartId){
        String jpqlQuery = "select r " +
                "from Ranking r " +
                "where chart_id = :chartId " +
                "and chart_type = :chartType";
        List<Ranking> rankings = em.createQuery(jpqlQuery
                , Ranking.class)
                .setParameter("chartId",chartId)
                .setParameter("chartType", chartType)
                .getResultList();
        return rankings;
    }

    /**
     * 랭킹 차트값 모두 조회
     * @param chartType
     * @return
     */

    public List<Ranking> findByChartAllId(String chartType){
        String jpqlQuery = "select r " +
                "from Ranking r " +
                "where chart_type = :chartType";

        List<Ranking> rankings = em.createQuery(jpqlQuery, Ranking.class)
                .setParameter("chartType", chartType)
                .getResultList();
        return rankings;
    }
}
