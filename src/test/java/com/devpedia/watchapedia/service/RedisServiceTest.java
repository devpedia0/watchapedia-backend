package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.exception.ValueNotMatchException;
import com.devpedia.watchapedia.repository.RedisRepository;
import com.devpedia.watchapedia.security.JwtTokenProvider;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class RedisServiceTest {

    @InjectMocks
    private RedisService redisService;
    @Mock
    private RedisRepository redisRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    public void getAccessToken_CorrectToken_ReturnToken() throws Exception {
        // given
        JwtTokenProvider.JwtParseInfo parseInfo = new JwtTokenProvider.JwtParseInfo(1L, Collections.singletonList("USER"));

        given(jwtTokenProvider.getUserParseInfo(anyString()))
                .willReturn(parseInfo);
        given(redisRepository.getRefreshToken(anyLong()))
                .willReturn("correctRefreshToken");
        given(jwtTokenProvider.createAccessToken(anyString(), anyList()))
                .willReturn("newToken");

        // when
        String newToken = redisService.getAccessTokenOrThrow("correctRefreshToken");

        // then
        assertThat(newToken).isNotBlank();
    }

    @Test
    public void getAccessToken_TokenParseFail_ThrowException() throws Exception {
        // given
        given(jwtTokenProvider.getUserParseInfo(anyString()))
                .willThrow(JwtException.class);

        // when
        Throwable throwable = catchThrowable(() -> redisService.getAccessTokenOrThrow("correctRefreshToken"));

        // then
        assertThat(throwable).isInstanceOf(JwtException.class);
    }

    @Test
    public void getAccessToken_StoredTokenIsNull_ThrowException() throws Exception {
        // given
        JwtTokenProvider.JwtParseInfo parseInfo = new JwtTokenProvider.JwtParseInfo(1L, Collections.singletonList("USER"));

        given(jwtTokenProvider.getUserParseInfo(anyString()))
                .willReturn(parseInfo);
        given(redisRepository.getRefreshToken(anyLong()))
                .willReturn(null);

        // when
        Throwable throwable = catchThrowable(() -> redisService.getAccessTokenOrThrow("correctRefreshToken"));

        // then
        assertThat(throwable).isInstanceOf(ValueNotMatchException.class);
    }

    @Test
    public void getAccessToken_NotMatchedToken_ThrowException() throws Exception {
        // given
        JwtTokenProvider.JwtParseInfo parseInfo = new JwtTokenProvider.JwtParseInfo(1L, Collections.singletonList("USER"));

        given(jwtTokenProvider.getUserParseInfo(anyString()))
                .willReturn(parseInfo);
        given(redisRepository.getRefreshToken(anyLong()))
                .willReturn("NotTheSameToken");

        // when
        Throwable throwable = catchThrowable(() -> redisService.getAccessTokenOrThrow("correctRefreshToken"));

        // then
        assertThat(throwable).isInstanceOf(ValueNotMatchException.class);
    }
}