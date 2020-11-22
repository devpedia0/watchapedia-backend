package com.devpedia.watchapedia.config;

import com.devpedia.watchapedia.security.JwtTokenProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .exposedHeaders(JwtTokenProvider.ACCESS_TOKEN_HEADER, JwtTokenProvider.REFRESH_TOKEN_HEADER);
    }
}
