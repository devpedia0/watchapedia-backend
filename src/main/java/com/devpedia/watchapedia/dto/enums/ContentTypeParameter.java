package com.devpedia.watchapedia.dto.enums;

import com.devpedia.watchapedia.domain.Book;
import com.devpedia.watchapedia.domain.Movie;
import com.devpedia.watchapedia.domain.TvShow;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentTypeParameter {
    MOVIES("M"),
    BOOKS("B"),
    TV_SHOWS("S");

    private String dtype;

    public static ContentTypeParameter from(String name) {
        return ContentTypeParameter.valueOf(name.toUpperCase());
    }
}
