package com.devpedia.watchapedia.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RankingChartTypeState {
    MOVIES("movies"),
    TV_SHOWS("tv_shows"),
    BOOKS("books");
    String chartType;
    public String value(){
        return chartType;
    }
}
