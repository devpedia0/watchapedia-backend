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

    /**
     * DTO class 의 필드값을 String 으로 가져와서,
     * Facebook 정보 요청 필드로 해서 페이스북 유저 정보를 조회한다.
     * @param dto 페이스북 요청 메타데이터에 해당하는 DTO
     * @param accessToken 페이스북 엑세스 토큰
     * @return 유저 정보를 담은 dto
     */
    public static <T> String buildFacebookUrl(Class<T> dto, String accessToken) {
        String params = Arrays.stream(dto.getDeclaredFields()).map(Field::getName).collect(Collectors.joining(","));
        return String.format("https://graph.facebook.com/me?fields=%s&access_token=%s", params, accessToken);
    }

    /**
     * DB에 저장되어 있는 S3 Bucket 내부의 경로를
     * 실제로 이미지를 조회할 수 있는 cloudfront url 로 변환한다.
     * @param filePath Bucket 내부 이미지 경로
     * @return cloudfront url
     */
    public static String getCloudFrontUrl(String filePath) {
        return CLOUDFRONT_DOMAIN + "/image/" + filePath;
    }

    public static String getCategorizedFilePath(String fileName, ImageCategory category) {
        return category.getDirectory() + "/" + fileName;
    }

}
