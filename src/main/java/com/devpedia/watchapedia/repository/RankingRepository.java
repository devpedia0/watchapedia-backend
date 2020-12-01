package com.devpedia.watchapedia.repository;

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
    public List<Ranking> findChartId(String chartType, String chartId){
        List<Ranking> rankings = em.createQuery("select r from Ranking r where chartId = :chartId and chartType = :chartType", Ranking.class)
                .setParameter("chartId",chartId)
                .setParameter("chartType", chartType)
                .getResultList();
        return rankings;
    }

}
