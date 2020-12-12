package com.devpedia.watchapedia.config;

import com.devpedia.watchapedia.controller.UserController;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameterConverter;
import com.devpedia.watchapedia.dto.enums.InterestContentOrderConverter;
import com.devpedia.watchapedia.dto.enums.RatingContentOrderConverter;
import com.devpedia.watchapedia.security.JwtTokenProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .exposedHeaders(
                        JwtTokenProvider.ACCESS_TOKEN_HEADER,
                        JwtTokenProvider.REFRESH_TOKEN_HEADER,
                        UserController.USER_ID_HEADER
                );
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new RatingContentOrderConverter());
        registry.addConverter(new InterestContentOrderConverter());
        registry.addConverter(new ContentTypeParameterConverter());
    }
}
