package com.devpedia.watchapedia.dto.enums;

public enum RatingContentOrder {
    AVG_SCORE,
    TITLE,
    NEW;

    public static RatingContentOrder from(String name) {
        return RatingContentOrder.valueOf(name.toUpperCase());
    }
}
