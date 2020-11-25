package com.devpedia.watchapedia.util;

import com.devpedia.watchapedia.domain.enums.ImageCategory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class UrlUtil {

    private static String CLOUDFRONT_DOMAIN;

    @Value("${cloud.aws.cloudfront.domain}")
    public void setCloudfrontDomain(String cloudfrontDomain) {
        UrlUtil.CLOUDFRONT_DOMAIN = cloudfrontDomain;
    }

    public static <T> String buildFacebookUrl(Class<T> dto, String accessToken) {
        String params = Arrays.stream(dto.getDeclaredFields()).map(Field::getName).collect(Collectors.joining(","));
        return String.format("https://graph.facebook.com/me?fields=%s&access_token=%s", params, accessToken);
    }

    public static String getCloudFrontUrl(String filePath) {
        return CLOUDFRONT_DOMAIN + "/image/" + filePath;
    }

    public static String getCategorizedFilePath(String fileName, ImageCategory category) {
        return category.getDirectory() + "/" + fileName;
    }

}
