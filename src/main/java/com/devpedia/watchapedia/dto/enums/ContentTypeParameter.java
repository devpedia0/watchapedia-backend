package com.devpedia.watchapedia.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentTypeParameter {
    MOVIES("M"),
    BOOKS("B"),
    TV_SHOWS("s");

    private String dtype;

    public static ContentTypeParameter from(String name) {
        return ContentTypeParameter.valueOf(name.toUpperCase());
    }
}
