package com.devpedia.watchapedia.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageCategory {
    POSTER("poster"),
    PARTICIPANT_PROFILE("pprofile"),
    USER_PROFILE("uprofile");

    private final String directory;
}
