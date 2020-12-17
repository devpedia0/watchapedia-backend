package com.devpedia.watchapedia.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RankingChartIdState {
    BOX_OFFICE("box_office"),
    NETFLIX("netflix"),
    MARS("mars");
    String chartId;
    public String value(){
        return chartId;
    }
    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(chartId);
    }

}


