package com.devpedia.watchapedia.security;

import io.jsonwebtoken.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${spring.jwt.secret-key}")
    private String secretKey;

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 60 * 60 * 1000L;
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    public static final String TYPE_ACCESS_TOKEN = "access";
    public static final String TYPE_REFRESH_TOKEN = "refresh";

    public static final String ACCESS_TOKEN_HEADER = "Authorization";
    public static final String REFRESH_TOKEN_HEADER = "RefreshToken";

    @PostConstruct
    protected void init() {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createAccessToken(String userPk, List<String> roles) {
        Claims claims = makeClaim(userPk, roles, TYPE_ACCESS_TOKEN);
        return createToken(claims, ACCESS_TOKEN_EXPIRE_TIME);
    }

    public String createRefreshToken(String userPk, List<String> roles) {
        Claims claims = makeClaim(userPk, roles, TYPE_REFRESH_TOKEN);
        return createToken(claims, REFRESH_TOKEN_EXPIRE_TIME);
    }

    private Claims makeClaim(String userPk, List<String> roles, String type) {
        Claims claims = Jwts.claims().setSubject(userPk);
        claims.put("roles", roles);
        claims.put("type", type);

        return claims;
    }

    public String createToken(Claims claims, long expiredTime) {
        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(now.getTime() + expiredTime))
                .setIssuedAt(now)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        JwtParseInfo parseInfo = getUserParseInfo(token);
        Credential credential = Credential.of(parseInfo);

        return new UsernamePasswordAuthenticationToken(credential, "", credential.getAuthorities());
    }

    public JwtParseInfo getUserParseInfo(String token) {
        Jws<Claims> parseInfo = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
        return JwtParseInfo.builder()
                .id(Long.valueOf(parseInfo.getBody().getSubject()))
                .roles(parseInfo.getBody().get("roles", List.class))
                .build();
    }

    public Long getUserPk(String token) throws JwtException {
        Jws<Claims> parseInfo = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
        return Long.valueOf(parseInfo.getBody().getSubject());
    }

    public String extractToken(HttpServletRequest request) {
        return request.getHeader(ACCESS_TOKEN_HEADER);
    }

    @Data
    public static class JwtParseInfo {
        private Long id;
        private List<String> roles;

        @Builder
        public JwtParseInfo(Long id, List<String> roles) {
            this.id = id;
            this.roles = roles;
        }
    }
}
