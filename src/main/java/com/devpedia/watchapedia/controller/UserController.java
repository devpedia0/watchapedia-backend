package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.exception.ExternalIOException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.RedisRepository;
import com.devpedia.watchapedia.repository.UserRepository;
import com.devpedia.watchapedia.security.JwtTokenProvider;
import com.devpedia.watchapedia.service.RedisService;
import com.devpedia.watchapedia.service.UserService;
import com.devpedia.watchapedia.util.UrlUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RedisRepository redisRepository;
    private final RedisService redisService;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @ApiResponses({
            @ApiResponse(code = 201, message = "회원가입 완료"),
            @ApiResponse(code = 400, message = "C001: 부적절한 입력값 \t\n C002: 이미 등록된 유저")
    })
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@RequestBody @Valid UserDto.SignupRequest request) {
        userService.join(
                User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .countryCode(request.getCountryCode())
                .build()
        );
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "엑세스 토큰과 리프레쉬 토큰을 헤더에 담아서 반환", responseHeaders = {
                    @ResponseHeader(name = JwtTokenProvider.ACCESS_TOKEN_HEADER, description = "엑세스 토큰", response = String.class),
                    @ResponseHeader(name = JwtTokenProvider.REFRESH_TOKEN_HEADER, description = "리프레쉬 토큰", response = String.class)
            }),
            @ApiResponse(code = 400, message = "C001: 부적절한 입력값 \t\n C003: 등록 안된 유저 \t\n C004: 비밀번호 불일치")
    })
    @PostMapping("/signin")
    public ResponseEntity<Void> signin(@RequestBody @Valid UserDto.SigninRequest request) {
        User user = userService.getMatchedUser(request.getEmail(), request.getPassword());

        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(user.getId()), user.getRoles());
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getId()), user.getRoles());

        redisRepository.addRefreshToken(user.getId(), refreshToken);

        return ResponseEntity.ok()
                .header(JwtTokenProvider.ACCESS_TOKEN_HEADER, accessToken)
                .header(JwtTokenProvider.REFRESH_TOKEN_HEADER, refreshToken)
                .build();
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "엑세스 토큰과 리프레쉬 토큰을 헤더에 담아서 반환", responseHeaders = {
                    @ResponseHeader(name = JwtTokenProvider.ACCESS_TOKEN_HEADER, description = "엑세스 토큰", response = String.class),
                    @ResponseHeader(name = JwtTokenProvider.REFRESH_TOKEN_HEADER, description = "리프레쉬 토큰", response = String.class)
            }),
            @ApiResponse(code = 400, message = "B001: 페이스북 오류 \t\n C001: 부적절한 입력값")
    })
    @PostMapping("/oauth/facebook")
    public ResponseEntity<Void> oauthFacebook(@RequestBody @Valid UserDto.OAuthTokenInfo tokenInfo) {
        String url = UrlUtil.buildFacebookUrl(UserDto.FacebookUserInfo.class, tokenInfo.getAccessToken());

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, JsonNode.class);

            UserDto.FacebookUserInfo userInfo =
                    objectMapper.treeToValue(response.getBody(), UserDto.FacebookUserInfo.class);

            userService.joinOAuthIfNotExist(userInfo.getEmail(), userInfo.getName());

            User user = userRepository.findByEmail(userInfo.getEmail());

            String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(user.getId()), user.getRoles());
            String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getId()), user.getRoles());

            redisRepository.addRefreshToken(user.getId(), refreshToken);

            return ResponseEntity.ok()
                    .header(JwtTokenProvider.ACCESS_TOKEN_HEADER, accessToken)
                    .header(JwtTokenProvider.REFRESH_TOKEN_HEADER, refreshToken)
                    .build();

        } catch (JsonProcessingException | RestClientException e) {
            throw new ExternalIOException(ErrorCode.OAUTH_PROVIDER_FAIL, "페이스북 인증에 실패했습니다");
        }
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "엑세스 토큰을 헤더에 담아서 반환", responseHeaders = {
                    @ResponseHeader(name = JwtTokenProvider.ACCESS_TOKEN_HEADER, description = "엑세스 토큰", response = String.class)
            }),
            @ApiResponse(code = 400, message = "C001: 리프레쉬 토큰 헤더 빈값 오류"),
            @ApiResponse(code = 401, message = "C005: 유효하지 않은 리프레쉬 토큰")
    })
    @PostMapping("/token")
    public ResponseEntity<Void> refreshAceessToken(
            @RequestHeader(name = JwtTokenProvider.REFRESH_TOKEN_HEADER) String refreshToken) {

        String accessToken = redisService.getAccessTokenOrThrow(refreshToken);

        return ResponseEntity.ok()
                .header(JwtTokenProvider.ACCESS_TOKEN_HEADER, accessToken)
                .build();
    }

    @GetMapping("/users/email")
    public UserDto.EmailCheckResult checkEmail(@RequestParam("email") @NotNull String email) {
        return userService.isExistEmail(email);
    }

    @GetMapping("/users/me")
    public UserDto.UserInfo getMyUserInfo(Principal principal) {
        Long id = Long.valueOf(principal.getName());
        return userService.getUserInfo(id);
    }
}