package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.builder.ContentMother;
import com.devpedia.watchapedia.builder.UserMother;
import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.AccessRange;
import com.devpedia.watchapedia.domain.enums.InterestState;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.dto.enums.InterestContentOrder;
import com.devpedia.watchapedia.dto.enums.RatingContentOrder;
import com.devpedia.watchapedia.exception.AccessDeniedException;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.ValueDuplicatedException;
import com.devpedia.watchapedia.exception.ValueNotMatchException;
import com.devpedia.watchapedia.repository.content.ContentRepository;
import com.devpedia.watchapedia.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ContentRepository contentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void join_NotDuplicated_SaveUser() throws Exception {
        // given
        UserDto.SignupRequest request = UserDto.SignupRequest.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .name("testName")
                .countryCode("KR")
                .build();

        given(userRepository.findFirstByEmail(anyString()))
                .willReturn(null);

        // when
        userService.join(request);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void join_Duplicated_ThrowException() throws Exception {
        // given
        UserDto.SignupRequest request = UserDto.SignupRequest.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .name("testName")
                .countryCode("KR")
                .build();

        User user = User.builder()
                .email("aaa@bb.ccc")
                .build();

        given(userRepository.findFirstByEmail(anyString()))
                .willReturn(user);

        // when
        Throwable throwable = catchThrowable(() -> userService.join(request));

        // then
        assertThat(throwable).isInstanceOf(ValueDuplicatedException.class);
    }

    @Test
    public void join_DeletedUser_ThrowException() throws Exception {
        // given
        UserDto.SignupRequest request = UserDto.SignupRequest.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .name("testName")
                .countryCode("KR")
                .build();

        User user = User.builder()
                .email("aaa@bb.ccc")
                .build();

        user.delete();

        given(userRepository.findFirstByEmail(anyString()))
                .willReturn(user);

        // when
        Throwable throwable = catchThrowable(() -> userService.join(request));

        // then
        assertThat(throwable).isInstanceOf(ValueDuplicatedException.class);
    }

    @Test
    public void joinOAuth_NotDuplicated_CreateAndSaveUser() throws Exception {
        // given
        given(userRepository.findFirstByEmail(anyString()))
                .willReturn(null);

        // when
        userService.joinOAuthIfNotExist("aaa@bb.ccc", "testName");

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void joinOAuth_Duplicated_Ignore() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .build();

        given(userRepository.findFirstByEmail(anyString()))
                .willReturn(user);

        // when
        userService.joinOAuthIfNotExist("aaa@bb.ccc", "testName");

        // then
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void joinOAuth_DeletedUser_ThrowException() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .build();

        user.delete();

        given(userRepository.findFirstByEmail(anyString()))
                .willReturn(user);

        // when
        Throwable throwable = catchThrowable(() -> userService.joinOAuthIfNotExist("aaa@bb.ccc", "testName"));

        // then
        assertThat(throwable).isInstanceOf(ValueDuplicatedException.class);
    }

    @Test
    public void matchUser_Correct_ReturnUser() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .build();

        given(userRepository.findFirstByEmail(anyString()))
                .willReturn(user);

        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(true);

        // when
        User matchedUser = userService.getMatchedUser("aaa@bb.ccc", "1234");

        // then
        assertThat(matchedUser).isNotNull();
    }

    @Test
    public void matchUser_UserNotFound_ThrowException() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .build();

        given(userRepository.findFirstByEmail(anyString()))
                .willReturn(null);

        // when
        Throwable throwable = catchThrowable(() -> userService.getMatchedUser("aaa@bb.ccc", "1234"));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }

    @Test
    public void matchUser_PasswordFail_ThrowException() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .build();

        given(userRepository.findFirstByEmail(anyString()))
                .willReturn(user);

        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(false);

        // when
        Throwable throwable = catchThrowable(() -> userService.getMatchedUser("aaa@bb.ccc", "1234"));

        // then
        assertThat(throwable).isInstanceOf(ValueNotMatchException.class);
    }

    @Test
    public void checkEmail_Exist_ReturnTrue() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .build();

        given(userRepository.findFirstByEmail(anyString()))
                .willReturn(user);

        // when
        UserDto.EmailCheckResult result = userService.isExistEmail("aaa@bb.ccc");

        // then
        assertThat(result.isExist()).isTrue();
    }

    @Test
    public void checkEmail_UserNotExist_ReturnFalse() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .build();

        given(userRepository.findFirstByEmail(anyString()))
                .willReturn(null);

        // when
        UserDto.EmailCheckResult result = userService.isExistEmail("ddd@fff.ggg");

        // then
        assertThat(result.isExist()).isFalse();
    }

    @Test
    public void getUserInfo_Exist_ReturnUserInfo() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .name("test")
                .countryCode("KR")
                .build();

        given(userRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // when
        UserDto.UserInfo userInfo = userService.getUserInfo(1L, 1L);

        // then
        assertThat(userInfo.getEmail()).isEqualTo(user.getEmail());
        assertThat(userInfo.getName()).isEqualTo(user.getName());
        assertThat(userInfo.getCountryCode()).isEqualTo(user.getCountryCode());
    }

    @Test
    public void getUserInfo_UserNotExist_ThrowException() throws Exception {
        // given
        given(userRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> userService.getUserInfo(1L, 1L));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }

    @Test
    public void editUserInfo_UserNotExist_ThrowException() throws Exception {
        // given
        UserDto.UserInfoEditRequest userInfo = UserDto.UserInfoEditRequest.builder()
                .build();

        given(userRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> userService.editUserInfo(1L, userInfo));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }

    @Test
    public void editUserInfo_OptionIsNull_PassSetting() throws Exception {
        // given
        User user = mock(User.class);

        UserDto.UserInfoEditRequest userInfo = UserDto.UserInfoEditRequest.builder()
                .build();

        given(userRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // when
        userService.editUserInfo(1L, userInfo);

        // then
        verify(user, times(0)).setName(anyString());
        verify(user, times(0)).setDescription(anyString());
        verify(user, times(0)).setCountryCode(anyString());
        verify(user, times(0)).setAccessRange(any(AccessRange.class));
        verify(user, times(0)).setSmsAgreed(anyBoolean());
        verify(user, times(0)).setEmailAgreed(anyBoolean());
        verify(user, times(0)).setPushAgreed(anyBoolean());
    }

    @Test
    public void editUserInfo_OptionIsNotNull_Setting() throws Exception {
        // given
        User user = mock(User.class);

        UserDto.UserInfoEditRequest userInfo = UserDto.UserInfoEditRequest.builder()
                .name("Park")
                .description("desc")
                .countryCode("KR")
                .accessRange(AccessRange.PUBLIC)
                .isEmailAgreed(true)
                .isPushAgreed(true)
                .isSmsAgreed(true)
                .build();

        given(userRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // when
        userService.editUserInfo(1L, userInfo);

        // then
        verify(user, times(1)).setName(anyString());
        verify(user, times(1)).setDescription(anyString());
        verify(user, times(1)).setCountryCode(anyString());
        verify(user, times(1)).setAccessRange(any(AccessRange.class));
        verify(user, times(1)).setSmsAgreed(anyBoolean());
        verify(user, times(1)).setEmailAgreed(anyBoolean());
        verify(user, times(1)).setPushAgreed(anyBoolean());
    }

    @Test
    public void delete_UserExist_Delete() throws Exception {
        // given
        User user = User.builder().build();

        given(userRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // when
        userService.delete(1L);

        // then
        assertThat(user.getIsDeleted()).isTrue();
    }

    @Test
    public void delete_UserNotExist_ThrowException() throws Exception {
        // given
        User user = null;

        given(userRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> userService.delete(1L));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }

    @Test
    public void getRatingInfo_PublicUser_ReturnInfo() throws Exception {
        // given
        User user = spy(UserMother.defaultUser().build());

        UserDto.UserActionCounts info = UserDto.UserActionCounts.builder()
                .book(UserDto.ActionCounts.zero())
                .movie(UserDto.ActionCounts.zero())
                .tvShow(UserDto.ActionCounts.zero())
                .build();

        given(user.getIsDeleted()).willReturn(false);
        given(userRepository.getUserActionCounts(anyLong())).willReturn(info);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        UserDto.UserActionCounts ratingInfo = userService.getRatingInfo(1L, 1L);

        // then
        assertThat(ratingInfo).isEqualTo(info);
    }

    @Test
    public void getRatingInfo_PrivateUserAndTokenMatch_ReturnInfo() throws Exception {
        // given
        User user = spy(UserMother.defaultUser().build());

        UserDto.UserActionCounts info = UserDto.UserActionCounts.builder()
                .book(UserDto.ActionCounts.zero())
                .movie(UserDto.ActionCounts.zero())
                .tvShow(UserDto.ActionCounts.zero())
                .build();

        given(user.getIsDeleted()).willReturn(false);
        given(user.getAccessRange()).willReturn(AccessRange.PRIVATE);
        given(userRepository.getUserActionCounts(anyLong())).willReturn(info);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        UserDto.UserActionCounts ratingInfo = userService.getRatingInfo(1L, 1L);

        // then
        assertThat(ratingInfo).isEqualTo(info);
    }

    @Test
    public void getRatingInfo_PrivateUserAndTokenMismatch_ThrowException() throws Exception {
        // given
        User user = spy(UserMother.defaultUser().build());

        given(user.getIsDeleted()).willReturn(false);
        given(user.getAccessRange()).willReturn(AccessRange.PRIVATE);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        Throwable throwable = catchThrowable(() -> userService.getRatingInfo(1L, 2L));

        // then
        assertThat(throwable).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void getRatingInfo_UserNotFound_ThrowException() throws Exception {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> userService.getRatingInfo(1L, 2L));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }

    @Test
    public void getRatingInfo_DeletedUser_ThrowException() throws Exception {
        // given
        User user = spy(UserMother.defaultUser().build());

        given(user.getIsDeleted()).willReturn(true);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        Throwable throwable = catchThrowable(() -> userService.getRatingInfo(1L, 2L));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }

    @Test
    public void getContentByRatingGroup_ScoreExist_ReturnInfo() throws Exception {
        // given
        UserDto.RatingContentParameter parameter = UserDto.RatingContentParameter.builder()
                .type(ContentTypeParameter.MOVIES)
                .order(RatingContentOrder.AVG_SCORE)
                .page(1)
                .size(10)
                .build();

        Map<String, Integer> ratingCounts = Map.of("0.5", 1);
        User user = UserMother.defaultUser().build();
        Movie movie = ContentMother.movie().build();
        Score score = Score.builder().user(user).content(movie).score(0.5).build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(userRepository.getGroupedScoreCount(anyLong(), any(ContentTypeParameter.class)))
                .willReturn(ratingCounts);
        given(userRepository.findUserGroupedScore(anyLong(), any(ContentTypeParameter.class), anyInt()))
                .willReturn(List.of(score));
        given(contentRepository.initializeAndUnproxy(any(Content.class))).willReturn(movie);

        // when
        Map<String, UserDto.UserRatingGroup> ratings = userService.getContentByRatingGroup(1L, 1L, parameter);

        // then
        assertThat(ratings).hasSize(10);
        assertThat(ratings.get("0.5")).isNotNull();
        assertThat(ratings.get("0.5").getCount()).isEqualTo(1);
        assertThat(ratings.get("0.5").getList()).hasSize(1);
    }

    @Test
    public void getContentByRating_ScoreExist_ReturnList() throws Exception {
        // given
        UserDto.RatingContentParameter parameter = UserDto.RatingContentParameter.builder()
                .type(ContentTypeParameter.MOVIES)
                .order(RatingContentOrder.AVG_SCORE)
                .page(1)
                .size(10)
                .build();

        User user = UserMother.defaultUser().build();
        Movie movie = ContentMother.movie().build();
        Score score = Score.builder().user(user).content(movie).score(0.5).build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(userRepository.findUserScores(anyLong(), any(ContentTypeParameter.class), anyDouble(),
                any(RatingContentOrder.class), any(Pageable.class))).willReturn(List.of(score));
        given(contentRepository.initializeAndUnproxy(any(Content.class))).willReturn(movie);

        // when
        List<ContentDto.MainListItem> list = userService.getContentByRating(1L, 1L, 0.5, parameter);

        // then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getScore()).isEqualTo(0.5);
    }

    @Test
    public void getContentByInterest_InterestExist_ReturnList() throws Exception {
        // given
        UserDto.InterestContentParameter parameter = UserDto.InterestContentParameter.builder()
                .type(ContentTypeParameter.MOVIES)
                .order(InterestContentOrder.AVG_SCORE)
                .state(InterestState.WISH)
                .page(1)
                .size(10)
                .build();

        User user = UserMother.defaultUser().build();
        Movie movie = ContentMother.movie().build();
        Interest interest = Interest.builder().user(user).content(movie).state(InterestState.WISH).build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(userRepository.findUserInterestContent(anyLong(), any(ContentTypeParameter.class), any(InterestState.class),
                any(InterestContentOrder.class), any(Pageable.class))).willReturn(List.of(interest));
        given(contentRepository.initializeAndUnproxy(any(Content.class))).willReturn(movie);

        // when
        List<ContentDto.MainListItem> list = userService.getContentByInterest(1L, 1L, parameter);

        // then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getScore()).isNull();
    }

    @Test
    public void getUserSearchList() throws Exception {
        // given
        User user = spy(UserMother.defaultUser().build());
        UserDto.ActionCounts actionCounts = UserDto.ActionCounts.builder()
                .ratingCount(1)
                .wishCount(1)
                .watchingCount(1)
                .notInterestCount(1)
                .commentCount(1)
                .build();

        given(user.getId()).willReturn(1L);
        given(userRepository.findByNameContaining(anyString(), any(Pageable.class)))
                .willReturn(List.of(user));
        given(userRepository.getActionCounts(anySet())).willReturn(Map.of(1L, actionCounts));

        // when
        List<UserDto.SearchUserItem> list = userService.getUserSearchList("name", PageRequest.of(0, 10));

        // then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(1L);
        assertThat(list.get(0).getCounts().getRatingCount()).isEqualTo(1);
        assertThat(list.get(0).getCounts().getWishCount()).isEqualTo(1);
        assertThat(list.get(0).getCounts().getWatchingCount()).isEqualTo(1);
        assertThat(list.get(0).getCounts().getNotInterestCount()).isEqualTo(1);
        assertThat(list.get(0).getCounts().getCommentCount()).isEqualTo(1);
    }
}