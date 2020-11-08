package com.devpedia.watchapedia.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UrlUtil {
    public static <T> String buildFacebookUrl(Class<T> dto, String accessToken) {
        String params = Arrays.stream(dto.getDeclaredFields()).map(Field::getName).collect(Collectors.joining(","));
        return String.format("https://graph.facebook.com/me?fields=%s&access_token=%s", params, accessToken);
    }
}
