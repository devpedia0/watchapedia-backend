package com.devpedia.watchapedia.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRepository {
    private static final String REFRESH_TOKEN_MAP_KEY = "RefreshToken";

    private final StringRedisTemplate redisTemplate;

    public void addRefreshToken(Long userId, String refreshToken) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(REFRESH_TOKEN_MAP_KEY, String.valueOf(userId), refreshToken);
    }

    public String getRefreshToken(Long userId) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return (String) hash.get(REFRESH_TOKEN_MAP_KEY, String.valueOf(userId));
    }
}
