package com.devpedia.watchapedia.dto.enums;

public enum InterestContentOrder {
    AVG_SCORE,
    TITLE,
    NEW,
    OLD;

    public static InterestContentOrder from(String name) {
        return InterestContentOrder.valueOf(name.toUpperCase());
    }
}
