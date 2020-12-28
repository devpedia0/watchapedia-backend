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

    /**
     * 리프레쉬 토큰 정상 여부 확인 후 엑세스 토큰을 재발급한다.
     * @param refreshToken 리프레쉬 토큰
     * @return 새로운 엑세스 토큰
     */
    public String getAccessTokenOrThrow(String refreshToken) {
        JwtTokenProvider.JwtParseInfo userInfo = jwtTokenProvider.getUserParseInfo(refreshToken);
        String storedToken = redisRepository.getRefreshToken(userInfo.getId());

        if (storedToken == null || !storedToken.equals(refreshToken))
            throw new ValueNotMatchException(ErrorCode.TOKEN_INVALID);

        return jwtTokenProvider.createAccessToken(String.valueOf(userInfo.getId()), userInfo.getRoles());
    }
}
