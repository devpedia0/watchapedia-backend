package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.domain.Ranking;
import com.devpedia.watchapedia.dto.RankingDto;
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
     * 랭킹 차트 아이디 & 차트 타입 조회
     * @param chartType 랭킹 차트 타입(movies, tvshows, books)
     * @param chartId 랭킹 차트 아이디(box_office,netflix,mars)
     * @return 랭킹 엔티티
     */
    public List<Ranking> findByChartId(String chartType, String chartId){

        String jpqlQuery = "select r.* " +
                "from ranking r " +
                "left join content c "+
                "on c.content_id = r.content_id " +
                "left join movie m " +
                "on c.content_id = m.content_id and :chartType ='movies' " +
                "left join tv_show t " +
                "on c.content_id = t.content_id and :chartType ='tvshows' " +
                "left join book b " +
                "on c.content_id = b.content_id and :chartType = 'books' " +
                "where r.chart_type = :chartType and r.chart_id = :chartId " +
                "order by r.chart_rank";

        return em.createNativeQuery(jpqlQuery
                , Ranking.class)
                .setParameter("chartType", chartType)
                .setParameter("chartId",chartId)
                .setFirstResult(0)
                .setMaxResults(30)
                .getResultList();
    }

    /**
     * 랭킹 차트값 모두 조회
     * @param chartType 랭킹 차트 타입(movies, tvshows, books)
     * @return 랭킹 엔티티
     */

    public List<Ranking> findByChartAllId(String chartType){
        String jpqlQuery1 = "select r.* " +
                "from ranking r " +
                "left join content c "+
                "on c.content_id = r.content_id " +
                "left join movie m " +
                "on c.content_id = m.content_id and :chartType ='movies' " +
                "left join tv_show t " +
                "on c.content_id = t.content_id and :chartType ='tvshows' " +
                "left join book b " +
                "on c.content_id = b.content_id and :chartType = 'books' " +
                "where r.chart_type = :chartType " +
                "order by r.chart_rank";

        return em.createNativeQuery(jpqlQuery1
                , Ranking.class)
                .setParameter("chartType", chartType)
                .setFirstResult(0)
                .setMaxResults(30)
                .getResultList();

    }
}
