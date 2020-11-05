package com.devpedia.watchapedia.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum AccessRange {
    PUBLIC("모두공개", 1),
    FRIEND("친구공개", 2),
    PRIVATE("비공개", 3);

    private String description;
    private int code;

    public static AccessRange ofCode(int code) {
        return Arrays.stream(AccessRange.values())
                .filter(v -> v.getCode() == code)
                .findAny()
                // TODO: Add exception
                .orElseThrow();

    }
}
