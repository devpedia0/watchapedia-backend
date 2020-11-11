package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.exception.ValueDuplicatedException;
import com.devpedia.watchapedia.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

}