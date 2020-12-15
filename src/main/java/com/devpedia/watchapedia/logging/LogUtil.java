package com.devpedia.watchapedia.logging;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.entries;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogUtil {

    public void logApi(HttpServletRequest request, HttpServletResponse response, JsonNode requestBody , JsonNode responseBody) {
        Map<String, Object> logs = new HashMap<>();

        logs.put("request_method", request.getMethod());
        logs.put("request_url", request.getRequestURI());
        logs.put("request_headers", buildHeadersMap(request));
        logs.put("request_parameters", buildParametersMap(request));
        logs.put("request_body", requestBody.toPrettyString());
        logs.put("response_status", response.getStatus());
        logs.put("response_headers", buildHeadersMap(response));
        logs.put("response_body", responseBody.toPrettyString());

        if (responseBody.has("status") && responseBody.has("code"))
            logs.put("response_error_code", responseBody.get("code").textValue());

        log.info("api log {}", entries(logs));
    }

    private Map<String, String> buildParametersMap(HttpServletRequest httpServletRequest) {
        Map<String, String> resultMap = new HashMap<>();
        Enumeration<String> parameterNames = httpServletRequest.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = httpServletRequest.getParameter(key);
            resultMap.put(key, value);
        }

        return resultMap;
    }

    private Map<String, String> buildHeadersMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

    private Map<String, String> buildHeadersMap(HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();

        Collection<String> headerNames = response.getHeaderNames();
        for (String header : headerNames) {
            map.put(header, response.getHeader(header));
        }

        return map;
    }
}
