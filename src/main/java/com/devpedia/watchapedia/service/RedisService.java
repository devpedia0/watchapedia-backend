package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.exception.ValueNotMatchException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.RedisRepository;
import com.devpedia.watchapedia.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisRepository redisRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public String getAccessTokenOrThrow(String refreshToken) {
        JwtTokenProvider.JwtParseInfo userInfo = jwtTokenProvider.getUserParseInfo(refreshToken);
        String storedToken = redisRepository.getRefreshToken(userInfo.getId());

        if (storedToken == null || !storedToken.equals(refreshToken))
            throw new ValueNotMatchException(ErrorCode.TOKEN_INVALID);

        return jwtTokenProvider.createAccessToken(String.valueOf(userInfo.getId()), userInfo.getRoles());
    }

    /**
     * 상세페이지 로그인 상태 체크
     * @param accessToken 유저 엑세스 토큰
     * @return 유저 아이디 값 반환
     */
    public Long getDetailAccessTokenOrThrow(String accessToken) {
        JwtTokenProvider.JwtParseInfo userInfo = jwtTokenProvider.getUserParseInfo(accessToken);
        return userInfo.getId();
    }


}
