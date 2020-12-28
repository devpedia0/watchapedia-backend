package com.devpedia.watchapedia.repository.user;

import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.domain.Interest;
import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.domain.Score;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.dto.enums.InterestContentOrder;
import com.devpedia.watchapedia.dto.enums.RatingContentOrder;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserCustomRepository {

    /**
     * 컨텐츠별 유저가 매긴 평점과 보고싶어요, 보는중, 관심없어요, 코멘트 개수를 구한다.
     * @param id user_id
     * @return 컨텐츠 별 유저 활동(평점, 보고싶어요, 보는중, 관심없어요, 코멘트) 개수
     */
    UserDto.UserActionCounts getUserActionCounts(Long id);

    /**
     * 유저가 평가한 평점(Score)을 구한다.
     * @param userId 조회 대상 유저
     * @param type 컨텐츠 타입 Enum
     * @param score 점수(0.5 ~ 5.0) null 이면 전체 조회
     * @param order 정렬 방식
     * @param pageable Pageable
     * @return 유저 평점 리스트
     */
    List<Score> findUserScores(Long userId, ContentTypeParameter type, Double score, RatingContentOrder order, Pageable pageable);

    /**
     * 유저가 평가한 평점을 평점(0.5 ~ 5.0)별로 개수만큼 가져온다.
     * 정렬은 작품 main_title 가나다 순 고정
     * @param userId 조회 대상 유저
     * @param type 컨텐츠 타입 Enum
     * @param size 각 평점별 조회 개수
     * @return 평점 리스트
     */
    List<Score> findUserGroupedScore(Long userId, ContentTypeParameter type, int size);

    /**
     * 평점(0.5 ~ 5.0)별 평가한 작품 개수를 Map 형태로 가져온다.
     * key: 평점, value: 작품 개수
     * @param userId 조회 대상 유저
     * @param type 컨텐츠 타입 Enum
     * @return 평점별 평가한 작품 개수
     */
    Map<String, Integer> getGroupedScoreCount(Long userId, ContentTypeParameter type);

    /**
     * 유저의 보고싶어요, 보는중, 관심없음에 해당하는 작품을 가져온다.
     * @param userId 조회 대상 유저
     * @param type 컨텐츠 타입 Enum
     * @param state 관심 종류 Enum
     * @param order 정렬 방식
     * @param pageable pageable
     * @return 관심 리스트
     */
    List<Interest> findUserInterestContent(Long userId, ContentTypeParameter type, InterestState state, InterestContentOrder order,
                                           Pageable pageable);

    /**
     * 유저 평점 분석 정보를 반환한다. 구성은
     * - 총 평점 개수
     * - 컨텐츠 별 평점 개수 (영화, 책, 티비)
     * - 평점 평균
     * - 많이 준 평점
     * - 평점 별 분포
     * @param id 조회 대상 유저 ID
     * @return 유저 평점 분석 정보
     */
    UserDto.UserRatingAnalysis getRatingAnalysis(Long id);

    /**
     * 유저의 직업 별 선호하는 인물 리스트를 구한다.
     * @param id 조회 대상 유저 ID
     * @param type 컨텐츠 타입 Enum
     * @param job 인물의 직업
     * @param size 사이즈
     * @return 선호하는 인물 리스트
     */
    List<UserDto.FavoritePerson> getFavoritePerson(Long id, ContentTypeParameter type, String job, int size);

    /**
     * 유저의 선호하는 태그 리스트를 구한다.
     * @param id 조회 대상 유저 ID
     * @param type 컨텐츠 타입 Enum
     * @param size 사이즈
     * @return 선호하는 태그 리스트
     */
    List<UserDto.FavoriteCommon> getFavoriteTag(Long id, ContentTypeParameter type, int size);

    /**
     * 유저의 선호하는 국가 리스트를 구한다.
     * 현재 영화에 대해서만 가능하다.
     * @param id 조회 대상 유저 ID
     * @param size 사이즈
     * @return 선호하는 국가 리스트
     */
    List<UserDto.FavoriteCommon> getFavoriteCountry(Long id, int size);

    /**
     * 유저의 선호하는 카테고리 리스트를 구한다.
     * 현재 카테고리는 테이블이 분리되어 있지 않고
     * 한 칼럼에 구분자(/)로 나눠서 들어가 있다.
     * 따라서 한 컨텐츠에 있는 최대 카테고리 개수(현재는 6)만큼
     * 구분자(/)로 잘라서 row 로 얻은 다음 처리하고 있음.
     * @param id 조회 대상 유저 ID
     * @param type 컨텐츠 타입 Enum
     * @param size 사이즈
     * @return 선호하는 카테고리 리스트
     */
    List<UserDto.FavoriteCommon> getFavoriteCategory(Long id, ContentTypeParameter type, int size);

    /**
     * 유저의 총 영화 감상 시간을 구한다.
     * 평점을 매긴 작품을 감상한 것으로 본다.
     * @param id 조회 대상 유저 ID
     * @return 총 영화 감상 시간
     */
    int getTotalRunningTime(Long id);

    /**
     * 해당 ID 유저의 전체 컨텐츠의
     * 평점, 코멘트, 보고싶어요, 보는중, 관심없음 개수를 구한다.
     * @param ids 검색 유저 ID Set
     * @return 유저 ID 별 ActionCounts
     */
    Map<Long, UserDto.ActionCounts> getActionCounts(Set<Long> ids);
}