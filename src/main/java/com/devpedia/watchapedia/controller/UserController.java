package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.dto.enums.InterestContentOrder;
import com.devpedia.watchapedia.dto.enums.RatingContentOrder;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class UserController {
    public static final String USER_ID_HEADER = "Id";

    private final UserService userService;
    private final UserRepository userRepository;
    private final RedisRepository redisRepository;
    private final RedisService redisService;

    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 유저 회원가입
     * @param request 유저 회원가입 정보
     */
    @PostMapping("/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@RequestBody @Valid UserDto.SignupRequest request) {
        userService.join(request);
    }

    /**
     * 회원 로그인.
     * 로그인 성공 시 Redis 에 리프레쉬 토큰 등록.
     * 엑세스 토큰, 리프레쉬 토큰, 유저 PK 헤더에 반환.
     * @param request 유저 로그인 정보
     * @return 엑세스 토큰, 리프레쉬 토큰, 유저 PK
     */
    @PostMapping("/auth/signin")
    public ResponseEntity<Void> signin(@RequestBody @Valid UserDto.SigninRequest request) {
        User user = userService.getMatchedUser(request.getEmail(), request.getPassword());

        String accessToken = jwtTokenProvider.createAccessToken(String.valueOf(user.getId()), user.getRoles());
        String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getId()), user.getRoles());

        redisRepository.addRefreshToken(user.getId(), refreshToken);

        return ResponseEntity.ok()
                .header(JwtTokenProvider.ACCESS_TOKEN_HEADER, accessToken)
                .header(JwtTokenProvider.REFRESH_TOKEN_HEADER, refreshToken)
                .header(USER_ID_HEADER, String.valueOf(user.getId()))
                .build();
    }

    /**
     * 페이스북 엑세스 토큰으로 페이스북 유저 정보를 가져온다.
     * 존재하지 않는 회원이면 DB에 저장한다.
     * 로그인 성공 시 Redis 에 리프레쉬 토큰 등록.
     * 엑세스 토큰, 리프레쉬 토큰, 유저 PK 헤더에 반환.
     * @param tokenInfo 페이스북 엑세스 토큰
     * @return 엑세스 토큰, 리프레쉬 토큰, 유저 PK
     */
    @PostMapping("/auth/facebook")
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
                    .header(USER_ID_HEADER, String.valueOf(user.getId()))
                    .build();

        } catch (JsonProcessingException | RestClientException e) {
            throw new ExternalIOException(ErrorCode.OAUTH_PROVIDER_FAIL, "페이스북 인증에 실패했습니다");
        }
    }

    /**
     * 리프레쉬 토큰으로 새로운 엑세스 토큰을 발급한다.
     * 리프레쉬 토큰이 Redis 에 정상 등록된 토큰이 아니라면
     * Throw Exception
     * @param refreshToken 리프레쉬 토큰
     * @return 엑세스 토큰
     */
    @PostMapping("/auth/token")
    public ResponseEntity<Void> refreshAccessToken(
            @RequestHeader(name = JwtTokenProvider.REFRESH_TOKEN_HEADER) @NotBlank String refreshToken) {

        String accessToken = redisService.getAccessTokenOrThrow(refreshToken);

        return ResponseEntity.ok()
                .header(JwtTokenProvider.ACCESS_TOKEN_HEADER, accessToken)
                .build();
    }

    /**
     * 회원가입 시 이메일 중복체크 용 API
     * @param email 검증하는 이메일
     * @return exist: true | false
     */
    @GetMapping("/public/email")
    public UserDto.EmailCheckResult checkEmail(@RequestParam("email") @NotNull String email) {
        return userService.isExistEmail(email);
    }

    /**
     * 유저정보를 가져온다.
     * name, email, description, countryCode, accessRange,
     * isEmailAgreed, isSmsAgreed, isPushAgreed, roles
     * @param targetId 조회 대상
     * @param principal 토큰 정보
     * @return 유저 정보
     */
    @GetMapping("/users/{id}")
    public UserDto.UserInfo getMyUserInfo(@PathVariable("id") Long targetId, @ApiIgnore Principal principal) {
        Long tokenId = principal != null ? Long.valueOf(principal.getName()) : null;
        return userService.getUserInfo(targetId, tokenId);
    }

    /**
     * 유저 정보를 변경한다.
     * DTO 의 값이 빈값이면 그 정보는 변경하지 않는다.
     * name, email, description, countryCode, accessRange,
     * isEmailAgreed, isSmsAgreed, isPushAgreed
     * @param request 유저 정보 변경
     * @param principal 토큰 정보
     */
    @PutMapping("/users/settings")
    public void editSettings(@RequestBody @Valid UserDto.UserInfoEditRequest request,
                             @ApiIgnore Principal principal) {
        Long id = principal != null ? Long.valueOf(principal.getName()) : null;
        userService.editUserInfo(id, request);
    }

    /**
     * 유저를 논리적으로 삭제한다.
     * delete_yn = true
     * @param principal 토큰 정보
     */
    @DeleteMapping("/users")
    public void deleteUser(@ApiIgnore Principal principal) {
        Long id = principal != null ? Long.valueOf(principal.getName()) : null;
        userService.delete(id);
    }

    /**
     * 유저의 각 매체별(영화, 책, TV)
     * 평점, 보고싶어요, 보는중, 관심없음, 코멘트 개수를 반환한다.
     * @param targetId 조회 대상 유저 ID
     * @param principal 유저 토큰 정보
     * @return 평점, 보고싶어요, 보는중, 관심없음, 코멘트 개수
     */
    @GetMapping("/users/{id}/ratings")
    public UserDto.UserActionCounts getRatingInfo(@PathVariable("id") Long targetId, @ApiIgnore Principal principal){
        Long tokenId = principal != null ? Long.valueOf(principal.getName()) : null;
        return userService.getRatingInfo(targetId, tokenId);
    }

    /**
     * 유저가 평가한 작품을 컨텐츠 별 정렬해서 반환한다.
     * @param targetId 조회 대상 유저
     * @param principal 토큰 정보
     * @param contentType 컨텐츠 타입(movies, books, tv_shows)
     * @param order 정렬 순서(평점평균=avg_score, 신작=new, 가나다=title)
     * @param page 페이지
     * @param size 사이즈
     * @return 유저가 평가한 작품 리스트
     */
    @GetMapping("/users/{id}/{contentType}/ratings")
    public List<ContentDto.MainListItem> getMovieRating(@PathVariable("id") Long targetId, @ApiIgnore Principal principal,
                                                        @PathVariable ContentTypeParameter contentType,
                                                        @RequestParam(required = false) RatingContentOrder order,
                                                        @RequestParam @Positive int page,
                                                        @RequestParam @Min(1)@Max(20) int size) {
        Long tokenId = principal != null ? Long.valueOf(principal.getName()) : null;
        UserDto.RatingContentParameter parameter = new UserDto.RatingContentParameter(contentType, order, page, size);
        return userService.getContentByRating(targetId, tokenId, null, parameter);
    }

    /**
     * 유저가 평가한 작품을 유저가 매긴 평점 별로 개수 및 리스트로 반환한다.
     * @param targetId 조회 대상 유저
     * @param principal 토큰 정보
     * @param contentType 컨텐츠 타입(movies, books, tv_shows)
     * @param size 평점 그룹 별 리스트 사이즈
     * @return 평점 그룹 별 개수 및 리스트
     */
    @GetMapping("/users/{id}/{contentType}/ratings/by_rating")
    public Map<Double, UserDto.UserRatingGroup> getMovieByRatingGroup(@PathVariable("id") Long targetId, @ApiIgnore Principal principal,
                                                                      @PathVariable ContentTypeParameter contentType,
                                                                      @RequestParam @Min(1) @Max(20) int size) {
        Long tokenId = principal != null ? Long.valueOf(principal.getName()) : null;
        UserDto.RatingContentParameter parameter = new UserDto.RatingContentParameter(contentType, null, 1, size);
        return userService.getContentByRatingGroup(targetId, tokenId, parameter);
    }

    /**
     * 유저가 평가한 작품 중 특정 평점을 준 작품 리스트를 조회한다.
     * @param targetId 조회 대상 유저
     * @param principal 토큰 정보
     * @param contentType 컨텐츠 타입(movies, books, tv_shows)
     * @param score 조회하려는 평점
     * @param page 페이지
     * @param size 사이즈
     * @return 특정 평점의 작품 리스트
     */
    @GetMapping("/users/{id}/{contentType}/ratings/{score}")
    public List<ContentDto.MainListItem> getMovieByRatings(@PathVariable("id") Long targetId, @ApiIgnore Principal principal,
                                                           @PathVariable ContentTypeParameter contentType,
                                                           @PathVariable Double score,
                                                           @RequestParam @Positive int page,
                                                           @RequestParam @Min(1) @Max(20) int size) {
        Long tokenId = principal != null ? Long.valueOf(principal.getName()) : null;
        UserDto.RatingContentParameter parameter = new UserDto.RatingContentParameter(contentType, RatingContentOrder.TITLE, page, size);
        return userService.getContentByRating(targetId, tokenId, score, parameter);
    }

    /**
     * 유저가 보고싶어요 표시한 작품을 정렬해서 반환한다.
     * @param targetId 조회 대상 유저
     * @param principal 토큰 정보
     * @param contentType 컨텐츠 타입(movies, books, tv_shows)
     * @param order 정렬 순서(평점평균=avg_score, 신작=new, 구작=old, 가나다=title)
     * @param page 페이지
     * @param size 사이즈
     * @return 보고싶어요 작품 리스트
     */
    @GetMapping("/users/{id}/{contentType}/wishes")
    public List<ContentDto.MainListItem> getMovieByWish(@PathVariable("id") Long targetId, @ApiIgnore Principal principal,
                                                        @PathVariable ContentTypeParameter contentType,
                                                        @RequestParam(required = false) InterestContentOrder order,
                                                        @RequestParam @Positive int page,
                                                        @RequestParam @Min(1) @Max(20) int size) {
        Long tokenId = principal != null ? Long.valueOf(principal.getName()) : null;
        UserDto.InterestContentParameter parameter =
                new UserDto.InterestContentParameter(contentType, InterestState.WISH, order, page, size);
        return userService.getContentByInterest(targetId, tokenId, parameter);
    }

    /**
     * 유저가 보는 중 표시한 작품을 정렬해서 반환한다.
     * @param targetId 조회 대상 유저
     * @param principal 토큰 정보
     * @param contentType 컨텐츠 타입(movies, books, tv_shows)
     * @param order 정렬 순서(평점평균=avg_score, 신작=new, 구작=old, 가나다=title)
     * @param page 페이지
     * @param size 사이즈
     * @return 보는중 작품 리스트
     */
    @GetMapping("/users/{id}/{contentType}/watchings")
    public List<ContentDto.MainListItem> getMovieByWatching(@PathVariable("id") Long targetId, @ApiIgnore Principal principal,
                                                            @PathVariable ContentTypeParameter contentType,
                                                            @RequestParam(required = false) InterestContentOrder order,
                                                            @RequestParam @Positive int page,
                                                            @RequestParam @Min(1) @Max(20) int size) {
        Long tokenId = principal != null ? Long.valueOf(principal.getName()) : null;
        UserDto.InterestContentParameter parameter =
                new UserDto.InterestContentParameter(contentType, InterestState.WATCHING, order, page, size);
        return userService.getContentByInterest(targetId, tokenId, parameter);
    }

    /**
     * 유저 취향분석 정보를 반환한다. 내용은
     * - 유저 평가정보 (컨텐츠 별 개수, 총 개수, 평균 점수, 많이 준 평점, 평점 분포)
     * - 영화 선호 (태그, 배우, 감독, 국가, 카테고리, 감상 시간)
     * - 책 선호 (태그, 작가)
     * 보다 자세한 필드정보는 DTO 참조할 것.
     * @param targetId 조회 대상 유저
     * @param principal 토큰 정보
     * @return 유저 취향분석 정보
     */
    @GetMapping("/users/{id}/analysis")
    public UserDto.UserAnalysisData getUserAnalysis(@PathVariable("id") Long targetId, @ApiIgnore Principal principal) {
        Long tokenId = principal != null ? Long.valueOf(principal.getName()) : null;
        return userService.getUserAnalysis(targetId, tokenId);
    }

    /**
     * 유저 검색 결과를 반환한다.
     * @param query 검색어
     * @param page 페이지
     * @param size 사이즈
     * @return 유저 검색 결과
     */
    @GetMapping("/public/searches/users")
    public List<UserDto.SearchUserItem> search(@RequestParam @NotBlank String query,
                                               @RequestParam @Positive int page,
                                               @RequestParam @Min(1)@Max(20) int size) {
        return userService.getUserSearchList(query, page, size);
    }
}