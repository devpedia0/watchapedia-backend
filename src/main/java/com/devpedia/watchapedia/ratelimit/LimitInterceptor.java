package com.devpedia.watchapedia.ratelimit;

import com.devpedia.watchapedia.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
@Component
public class LimitInterceptor extends HandlerInterceptorAdapter {

    private final RedisRepository redisRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!redisRepository.isAllowed(request.getRemoteAddr())) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return false;
        } else{
            return true;
        }
    }
}
