package com.devpedia.watchapedia.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoggingInterceptor extends HandlerInterceptorAdapter {

    private final ObjectMapper mapper;
    private final LogUtil logUtil;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (request instanceof ContentCachingRequestWrapper && response instanceof ContentCachingResponseWrapper) {
            final ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper) request;
            final ContentCachingResponseWrapper cachingResponse = (ContentCachingResponseWrapper) response;

            JsonNode requestBody = mapper.readTree(cachingRequest.getContentAsByteArray());
            JsonNode responseBody = mapper.readTree(cachingResponse.getContentAsByteArray());

            logUtil.logApi(request, response, requestBody, responseBody);
        }
    }
}
