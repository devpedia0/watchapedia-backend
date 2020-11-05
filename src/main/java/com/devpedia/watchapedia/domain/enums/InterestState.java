package com.devpedia.watchapedia.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum InterestState {
    WISH("보고싶어요", 1),
    WATCHING("보는중", 2),
    NOT_INTEREST("관심없음", 3);

    private String description;
    private int code;

    public static InterestState ofCode(int code) {
        return Arrays.stream(InterestState.values())
                .filter(v -> v.getCode() == code)
                .findAny()
                // TODO: Add exception
                .orElseThrow();

    }
}
