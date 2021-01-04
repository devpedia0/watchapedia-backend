package com.devpedia.watchapedia.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisRepository {
    private static final String REFRESH_TOKEN_MAP_KEY = "RefreshToken";

    private static final int EXPIRE_SECONDS = 5;
    private static final int REQUESTS_PER_EXPIRE = 50;

    private final StringRedisTemplate redisTemplate;

    public void addRefreshToken(Long userId, String refreshToken) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        hash.put(REFRESH_TOKEN_MAP_KEY, String.valueOf(userId), refreshToken);
    }

    public String getRefreshToken(Long userId) {
        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
        return (String) hash.get(REFRESH_TOKEN_MAP_KEY, String.valueOf(userId));
    }

    public boolean isAllowed(String ip) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String requests = operations.get(ip);
        if (StringUtils.isNotBlank(requests) && Integer.parseInt(requests) >= REQUESTS_PER_EXPIRE) {
            return false;
        }
        List<Object> txResults = redisTemplate.execute(new SessionCallback<>() {
            @Override
            public <K, V> List<Object> execute(RedisOperations<K, V> operations) throws DataAccessException {
                final StringRedisTemplate redisTemplate = (StringRedisTemplate) operations;
                final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
                operations.multi();
                valueOperations.increment(ip);
                redisTemplate.expire(ip, EXPIRE_SECONDS, TimeUnit.SECONDS);
                return operations.exec();
            }
        });
        return true;
    }
}
