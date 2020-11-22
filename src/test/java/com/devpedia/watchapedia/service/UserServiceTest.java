package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.*;
import com.devpedia.watchapedia.domain.enums.AccessRange;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.ValueDuplicatedException;
import com.devpedia.watchapedia.exception.ValueNotMatchException;
import com.devpedia.watchapedia.repository.ContentRepository;
import com.devpedia.watchapedia.repository.UserRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
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

        given(userRepository.findByEmail(anyString()))
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

        given(userRepository.findByEmail(anyString()))
                .willReturn(user);

        // when
        Throwable throwable = catchThrowable(() -> userService.join(request));

        // then
        assertThat(throwable).isInstanceOf(ValueDuplicatedException.class);
    }

    @Test
    public void joinOAuth_NotDuplicated_CreateAndSaveUser() throws Exception {
        // given
        given(userRepository.findByEmail(anyString()))
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

        given(userRepository.findByEmail(anyString()))
                .willReturn(user);

        // when
        userService.joinOAuthIfNotExist("aaa@bb.ccc", "testName");

        // then
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    public void matchUser_Correct_ReturnUser() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .build();

        given(userRepository.findByEmail(anyString()))
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

        given(userRepository.findByEmail(anyString()))
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

        given(userRepository.findByEmail(anyString()))
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

        given(userRepository.findByEmail(anyString()))
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

        given(userRepository.findByEmail(anyString()))
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
                .willReturn(user);

        // when
        UserDto.UserInfo userInfo = userService.getUserInfo(1L);

        // then
        assertThat(userInfo.getEmail()).isEqualTo(user.getEmail());
        assertThat(userInfo.getName()).isEqualTo(user.getName());
        assertThat(userInfo.getCountryCode()).isEqualTo(user.getCountryCode());
    }

    @Test
    public void getUserInfo_UserNotExist_ThrowException() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .name("test")
                .countryCode("KR")
                .build();

        given(userRepository.findById(anyLong()))
                .willReturn(null);

        // when
        Throwable throwable = catchThrowable(() -> userService.getUserInfo(1L));

        // then
        assertThat(throwable).isInstanceOf(EntityNotExistException.class);
    }

    @Test
    public void editUserInfo_UserNotExist_ThrowException() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .name("test")
                .countryCode("KR")
                .build();

        UserDto.UserInfoEditRequest userInfo = UserDto.UserInfoEditRequest.builder()
                .build();

        given(userRepository.findById(anyLong()))
                .willReturn(null);

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
                .willReturn(user);

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
                .willReturn(user);

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
    public void findAll_HasUsers_ReturnList() throws Exception {
        // given
        List<User> users = Arrays.asList(
                User.builder().build(),
                User.builder().build(),
                User.builder().build()
        );

        given(userRepository.findAll())
                .willReturn(users);

        // when
        List<UserDto.UserInfo> actualList = userService.getAllUserInfo();

        // then
        assertThat(actualList).hasSize(3);
    }

    @Test
    public void findAll_HasNoUsers_ReturnEmptyList() throws Exception {
        // given
        given(userRepository.findAll())
                .willReturn(new ArrayList<>());

        // when
        List<UserDto.UserInfo> actualList = userService.getAllUserInfo();

        // then
        assertThat(actualList).isNotNull().hasSize(0);
    }

    @Test
    public void addCollection_Correct_SaveCollection() throws Exception {
        // given
        User user = User.builder().build();

        List<Content> contents = Arrays.asList(
                Movie.builder().build(),
                TvShow.builder().build(),
                Book.builder().build()
        );

        UserDto.CollectionInsertRequest request = UserDto.CollectionInsertRequest.builder()
                .title("title")
                .description("desc")
                .contents(new ArrayList<>())
                .build();

        given(userRepository.findById(anyLong()))
                .willReturn(user);
        given(contentRepository.findListIn(any(Class.class), anySet()))
                .willReturn(contents);

        // when
        userService.addCollection(1L, request);

        // then
        verify(userRepository, times(1)).save(any(Collection.class));
        verify(userRepository, times(contents.size())).save(any(CollectionContent.class));
    }
}