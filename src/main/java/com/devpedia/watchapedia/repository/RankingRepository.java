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

    public List<Ranking> findChartId(String chart_type, String chart_id){
        List<Ranking> rankings = em.createQuery("select r from Ranking r where chart_id = :chart_id and chart_type = :chart_type", Ranking.class)
                .setParameter("chart_id",chart_id)
                .setParameter("chart_type", chart_type)
                .getResultList();
        return rankings;
    }
}
