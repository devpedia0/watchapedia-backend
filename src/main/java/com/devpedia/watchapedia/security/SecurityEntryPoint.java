package com.devpedia.watchapedia.security;


import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.exception.common.ErrorResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class SecurityEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 401
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void commence(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException {
        // 403
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
