package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.AccessRange;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.exception.AccessDeniedException;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.ValueDuplicatedException;
import com.devpedia.watchapedia.exception.ValueNotMatchException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.exception.common.ErrorField;
import com.devpedia.watchapedia.repository.content.ContentRepository;
import com.devpedia.watchapedia.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    public static final int FAVORITE_LIST_SIZE = 10;

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 유저 회원가입을 한다.
     * 해당 이메일의 유저가 존재하거나 삭제된 회원이면 Exception
     * @param request 유저가입 정보
     */
    public void join(UserDto.SignupRequest request) {
        User existUser = userRepository.findFirstByEmail(request.getEmail());

        if (existUser != null) {
            if (existUser.getIsDeleted())
                throw new ValueDuplicatedException(ErrorCode.USER_ON_DELETE, ErrorField.of("email", request.getEmail(), ""));

            throw new ValueDuplicatedException(ErrorCode.USER_DUPLICATED, ErrorField.of("email", request.getEmail(), ""));
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .countryCode(request.getCountryCode())
                .build();

        userRepository.save(user);
    }

    /**
     * OAuth 진행 시 해당 이메일의 유저가
     * 존재하면 bypass
     * 존재하지 않으면 가입시키고
     * 삭제된 회원이면 Exception
     * @param email 가입 이메일
     * @param name 가입 이름
     */
    public void joinOAuthIfNotExist(String email, String name) {
        User existUser = userRepository.findFirstByEmail(email);

        if (existUser != null) {
            if (existUser.getIsDeleted())
                throw new ValueDuplicatedException(ErrorCode.USER_ON_DELETE, ErrorField.of("email", email, ""));

            return;
        }

        User user = User.builder()
                .email(email)
                .password(UUID.randomUUID().toString())
                .name(name)
                .countryCode("KR")
                .build();

        userRepository.save(user);
    }

    /**
     * 이메일과 비밀번호가 일치하는 유저를 조회한다.
     * 해당 이메일 유저가 존재하지 않거나
     * 비밀번호가 일치하지 않으면 Exception
     * @param email 이메일
     * @param password 비밀번호
     * @return 이메일과 비밀번호가 일치하는 유저
     */
    public User getMatchedUser(String email, String password) {
        User user = getUserIfExistOrThrow(email);
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new ValueNotMatchException(ErrorCode.PASSWORD_NOT_MATCH);

        return user;
    }

    /**
     * 해당 이메일로 가입된 유저의 여부를 반환한다.
     * @param email 찾을 이메일
     * @return exist = true, false
     */
    public UserDto.EmailCheckResult isExistEmail(String email) {
        return UserDto.EmailCheckResult.builder()
                .isExist(isExistUser(email))
                .build();
    }

    /**
     * 해당 이메일로 가입된 유저가 있는지 조회한다.
     * @param email 찾을 이메일
     * @return 존재 여부
     */
    private boolean isExistUser(String email) {
        User user = userRepository.findFirstByEmail(email);
        return user != null;
    }

    /**
     * 유저정보 공개범위에 따라 요청의 권한 여부를 체크하고
     * 유저 이메일, 이름, 설명, 국가코드, 각종 설정, 권한 등을 반환한다.
     * @param targetId 조회하고자하는 유저 ID
     * @param tokenId 토큰에 담긴 유저 ID
     * @return 유저 정보
     */
    public UserDto.UserInfo getUserInfo(Long targetId, Long tokenId) {
        User user = getUserIfExistOrThrow(targetId);
        if (isAccessNotAvailable(targetId, tokenId))
            throw new AccessDeniedException(ErrorCode.ACCESS_NOT_AVAILABLE, "해당 유저는 비공개 유저입니다.");
        return new UserDto.UserInfo(user);
    }

    /**
     * 유저가 설정한 공개범위에 따라 해당 토큰의 조회가능 여부를 반환한다.
     * @param targetUserId 조회하고자하는 유저
     * @param tokenUserId 토큰에 담긴 유저
     * @return 조회가능 여부
     */
    private boolean isAccessNotAvailable(Long targetUserId, Long tokenUserId) {
        User targetUser = getUserIfExistOrThrow(targetUserId);
        return targetUser.getAccessRange() == AccessRange.PRIVATE && !targetUserId.equals(tokenUserId);
    }

    /**
     * 수정할 수 있는 유저 정보를 수정한다.
     * 파라미터 값이 비어있으면 그 항목은 수정에서 제외한다.
     * user_id 가 존재하지 않으면 Exception
     * @param id user_id
     * @param userInfo 유저 수정 정보
     */
    public void editUserInfo(Long id, UserDto.UserInfoEditRequest userInfo) {
        User user = getUserIfExistOrThrow(id);

        if (userInfo.getName() != null && !StringUtils.isBlank(userInfo.getName()))
            user.setName(userInfo.getName());
        if (userInfo.getDescription() != null) user.setDescription(userInfo.getDescription());
        if (userInfo.getCountryCode() != null) user.setCountryCode(userInfo.getCountryCode());
        if (userInfo.getAccessRange() != null) user.setAccessRange(userInfo.getAccessRange());
        if (userInfo.getIsEmailAgreed() != null) user.setEmailAgreed(userInfo.getIsEmailAgreed());
        if (userInfo.getIsPushAgreed() != null) user.setPushAgreed(userInfo.getIsPushAgreed());
        if (userInfo.getIsSmsAgreed() != null) user.setSmsAgreed(userInfo.getIsSmsAgreed());
    }

    /**
     * 유저를 삭제한다.
     * 물리적 삭제가 아니라 플래그 값만 변경함.
     * 유저가 존재하지 않으면 Exception
     * @param id user_id
     */
    public void delete(Long id) {
        User user = getUserIfExistOrThrow(id);
        user.delete();
    }

    /**
     * 해당 user_id 로 유저를 조회한다.
     * 존재하지 않거나 삭제된 유저라면 Exception
     * @param id user_id
     * @return 유저
     */
    public User getUserIfExistOrThrow(Long id) {
        if (id == null) throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        Optional<User> optionalUser = userRepository.findById(id);
        User user = optionalUser.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        if (user.getIsDeleted())
            throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        return user;
    }

    /**
     * 해당 이메일로 유저를 조회한다.
     * 존재하지 않거나 삭제된 유저라면 Exception
     * @param email 이메일
     * @return 유저
     */
    public User getUserIfExistOrThrow(String email) {
        if (email == null) throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        User user = userRepository.findFirstByEmail(email);
        if (user == null || user.getIsDeleted())
            throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        return user;
    }

    /**
     * 해당 아이디의 유저가 매긴
     * 컨텐츠 별 활동 개수를 반환한다.
     * @param targetId 조회 대상 유저 ID
     * @param tokenId 토큰 유저 ID
     * @return 컨텐츠 별 유저 활동 개수
     */
    public UserDto.UserActionCounts getRatingInfo(Long targetId, Long tokenId) {
        if (isAccessNotAvailable(targetId, tokenId))
            throw new AccessDeniedException(ErrorCode.ACCESS_NOT_AVAILABLE, "해당 유저는 비공개 유저입니다.");
        return userRepository.getUserActionCounts(targetId);
    }

    /**
     * 유저가 평점을 매긴 작품을 평점 그룹(0.5 ~ 5.0) 별로
     * 해당하는 작품 개수와 작품 리스트를 화면용 DTO 리스트 형태로 가져온다.
     * @param targetId 조회 대상 유저
     * @param tokenId 토큰 정보
     * @param parameter type, order, page, size
     * @return 평점 그룹 별 개수 및 리스트
     */
    public Map<String, UserDto.UserRatingGroup> getContentByRatingGroup(Long targetId, Long tokenId,
                                                                        UserDto.RatingContentParameter parameter) {
        if (isAccessNotAvailable(targetId, tokenId))
            throw new AccessDeniedException(ErrorCode.ACCESS_NOT_AVAILABLE, "해당 유저는 비공개 유저입니다.");

        Map<String, Integer> counts = userRepository.getGroupedScoreCount(targetId, parameter.getType());
        List<Score> scores = userRepository.findUserGroupedScore(targetId, parameter.getType(), parameter.getSize());

        Map<String, UserDto.UserRatingGroup> result = new LinkedHashMap<>();

        for (double score = 0.5; score <= 5; score += 0.5) {
            result.putIfAbsent(String.valueOf(score), UserDto.UserRatingGroup.builder()
                    .count(counts.getOrDefault(String.valueOf(score), 0))
                    .list(new ArrayList<>())
                    .build());
        }

        for (Score score : scores) {
            Content content = contentRepository.initializeAndUnproxy(score.getContent());
            result.get(String.valueOf(score.getScore()))
                    .getList().add(ContentDto.MainListItem.of(content, score.getScore()));
        }

        return result;
    }

    /**
     * 유저가 평점을 매긴 작품들을 정렬해서
     * 화면에서 보여주기 위한 DTO 리스트 형태로 가져온다.
     * score 가 null 이면 전체 점수에 해당하는 작품 조회,
     * 혹은 0.5 ~ 5.0 각각에 해당하는 작품 조회.
     * @param targetId 조회 대상 유저
     * @param tokenId 토큰 정보
     * @param score 조회 점수(null 이면 전체 조회)
     * @param parameter type, order, page, size
     * @return 작품 리스트
     */
    public List<ContentDto.MainListItem> getContentByRating(Long targetId, Long tokenId, Double score,
                                                            UserDto.RatingContentParameter parameter) {
        if (isAccessNotAvailable(targetId, tokenId))
            throw new AccessDeniedException(ErrorCode.ACCESS_NOT_AVAILABLE, "해당 유저는 비공개 유저입니다.");

        List<Score> scores = userRepository.findUserScores(targetId, parameter.getType(), score,
                parameter.getOrder(), PageRequest.of(parameter.getPage() - 1, parameter.getSize()));

        return scores.stream()
                .map(s -> ContentDto.MainListItem.of(contentRepository.initializeAndUnproxy(s.getContent()), s.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * 유저가 보고싶어요, 보는중, 관심없음을 매긴 작품들을 정렬해서
     * 화면에서 보여주기 위한 DTO 리스트 형태로 가져온다.
     * @param targetId 조회 대상 유저
     * @param tokenId 토큰 정보
     * @param parameter type, order, state(보는중, 보고싶어요, 관심없음), page, size
     * @return 해당 관심종류 리스트
     */
    public List<ContentDto.MainListItem> getContentByInterest(Long targetId, Long tokenId,
                                                              UserDto.InterestContentParameter parameter) {
        if (isAccessNotAvailable(targetId, tokenId))
            throw new AccessDeniedException(ErrorCode.ACCESS_NOT_AVAILABLE, "해당 유저는 비공개 유저입니다.");

        List<Interest> interests = userRepository.findUserInterestContent(targetId, parameter.getType(), parameter.getState(),
                parameter.getOrder(), PageRequest.of(parameter.getPage() - 1, parameter.getSize()));

        return interests.stream()
                .map(i -> ContentDto.MainListItem.of(contentRepository.initializeAndUnproxy(i.getContent()), null))
                .collect(Collectors.toList());
    }

    /**
     * 유저 취향분석 정보를 반환한다. 내용은
     * - 유저 평가정보 (컨텐츠 별 개수, 총 개수, 평균 점수, 많이 준 평점, 평점 분포)
     * - 영화 선호 (태그, 배우, 감독, 국가, 카테고리, 감상 시간)
     * - 책 선호 (태그, 작가)
     * @param targetId 조회 대상 유저
     * @param tokenId 토큰 정보
     * @return 유저 취향분석 정보
     */
    public UserDto.UserAnalysisData getUserAnalysis(Long targetId, Long tokenId) {
        if (isAccessNotAvailable(targetId, tokenId))
            throw new AccessDeniedException(ErrorCode.ACCESS_NOT_AVAILABLE, "해당 유저는 비공개 유저입니다.");

        User user = getUserIfExistOrThrow(targetId);

        UserDto.UserRatingAnalysis ratingAnalysis = userRepository.getRatingAnalysis(targetId);
        UserDto.UserMovieAnalysis movieAnalysis = getMovieAnalysis(targetId);
        UserDto.UserBookAnalysis bookAnalysis = getBookAnalysis(targetId);

        return UserDto.UserAnalysisData.builder()
                .userName(user.getName())
                .rating(ratingAnalysis)
                .movie(movieAnalysis)
                .book(bookAnalysis)
                .build();
    }

    /**
     * 영화 선호 정보를 반환한다. 내용은
     * - 선호 태그
     * - 선호 배우, 감독
     * - 선호 국가
     * - 선호 카테고리
     * - 총 영화 감상 시간
     * @param userId 조회 대상 유저
     * @return 영화 선호 정보
     */
    private UserDto.UserMovieAnalysis getMovieAnalysis(Long userId) {
        List<UserDto.FavoriteCommon> tags = userRepository.getFavoriteTag(userId, ContentTypeParameter.MOVIES, FAVORITE_LIST_SIZE);
        List<UserDto.FavoriteCommon> countries = userRepository.getFavoriteCountry(userId, FAVORITE_LIST_SIZE);
        List<UserDto.FavoriteCommon> categories = userRepository.getFavoriteCategory(userId, ContentTypeParameter.MOVIES, FAVORITE_LIST_SIZE);
        List<UserDto.FavoritePerson> actor = userRepository.getFavoritePerson(userId, ContentTypeParameter.MOVIES, "배우", FAVORITE_LIST_SIZE);
        List<UserDto.FavoritePerson> director = userRepository.getFavoritePerson(userId, ContentTypeParameter.MOVIES, "감독", FAVORITE_LIST_SIZE);
        int totalRunningTimeInMinute = userRepository.getTotalRunningTime(userId);

        return UserDto.UserMovieAnalysis.builder()
                .tag(tags)
                .country(countries)
                .category(categories)
                .actor(actor)
                .director(director)
                .totalRunningTimeInMinute(totalRunningTimeInMinute)
                .build();
    }

    /**
     * 책 선호 정보를 반환한다. 내용은
     * - 선호 태그
     * - 선호 작가
     * @param userId 조회 대상 유저
     * @return 책 선호 정보
     */
    private UserDto.UserBookAnalysis getBookAnalysis(Long userId) {
        List<UserDto.FavoriteCommon> tags = userRepository.getFavoriteTag(userId, ContentTypeParameter.BOOKS, FAVORITE_LIST_SIZE);
        List<UserDto.FavoritePerson> author = userRepository.getFavoritePerson(userId, ContentTypeParameter.BOOKS, "저자", FAVORITE_LIST_SIZE);

        return UserDto.UserBookAnalysis.builder()
                .tag(tags)
                .author(author)
                .build();
    }

    /**
     * 유저를 이름으로 검색한 뒤 해당 유저들의
     * 평점, 코멘트, 보고싶어요, 보는중, 관심없음 개수를 담아서
     * 검색 결과 DTO 로 반환한다.
     * @param query 검색어(user.name)
     * @param pageable pageable
     * @return 유저 검색 결과
     */
    public List<UserDto.SearchUserItem> getUserSearchList(String query, Pageable pageable) {
        List<User> users = userRepository.findByNameContaining(query, pageable);
        Map<Long, UserDto.ActionCounts> actionCountsMap = userRepository.getActionCounts(getUserIds(users));
        return users.stream()
                .map(user -> UserDto.SearchUserItem.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .description(user.getDescription())
                        .counts(actionCountsMap.get(user.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 유저 리스트에서 각 유저의 ID 를 추출한다.
     * @param users 유저
     * @return 유저 ID Set
     */
    private Set<Long> getUserIds(List<User> users) {
        return users.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
    }
}