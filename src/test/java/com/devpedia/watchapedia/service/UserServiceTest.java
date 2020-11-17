package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.ValueDuplicatedException;
import com.devpedia.watchapedia.exception.ValueNotMatchException;
import com.devpedia.watchapedia.repository.UserRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private PasswordEncoder passwordEncoder;

    @Test
    public void join_NotDuplicated_SaveUser() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .build();

        given(userRepository.findByEmail(anyString()))
                .willReturn(null);

        // when
        userService.join(user);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void join_Duplicated_ThrowException() throws Exception {
        // given
        User user = User.builder()
                .email("aaa@bb.ccc")
                .build();

        given(userRepository.findByEmail(anyString()))
                .willReturn(user);

        // when
        Throwable throwable = catchThrowable(() -> userService.join(user));

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
    public void checkEmail_NotExist_ReturnFalse() throws Exception {
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
    public void getUserInfo_NotExist_ThrowException() throws Exception {
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
}