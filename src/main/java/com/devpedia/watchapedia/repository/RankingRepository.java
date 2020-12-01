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

    public List<Ranking> getRankingList(){
        List<Ranking> res = new ArrayList<>();
        return res;
    }

}
