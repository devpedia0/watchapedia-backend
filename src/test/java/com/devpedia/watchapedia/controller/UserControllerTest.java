package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.exception.ValueDuplicatedException;
import com.devpedia.watchapedia.exception.ValueNotMatchException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.exception.handler.BusinessExceptionHandler;
import com.devpedia.watchapedia.exception.handler.CommonExceptionHandler;
import com.devpedia.watchapedia.repository.RedisRepository;
import com.devpedia.watchapedia.repository.UserRepository;
import com.devpedia.watchapedia.security.JwtTokenProvider;
import com.devpedia.watchapedia.service.RedisService;
import com.devpedia.watchapedia.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private ObjectMapper objectMapper;
    @SpyBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RedisRepository redisRepository;
    @MockBean
    private RedisService redisService;
    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void signup_UserExist_Status400AndC002() throws Exception {
        // given
        UserDto.SignupRequest request = UserDto.SignupRequest.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .name("testName")
                .countryCode("KR")
                .build();

        willThrow(new ValueDuplicatedException(ErrorCode.USER_DUPLICATED))
                .given(userService).join(any(UserDto.SignupRequest.class));

        // when
        ResultActions actions = mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C002"));
    }

    @Test
    public void signup_InvalidParams_Status400AndC001() throws Exception {
        // given
        UserDto.SignupRequest request = UserDto.SignupRequest.builder()
                .build();

        // when
        ResultActions actions = mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        verify(userService, times(0)).join(any(UserDto.SignupRequest.class));
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    public void signup_DeletedUser_Status400AndC006() throws Exception {
        // given
        UserDto.SignupRequest request = UserDto.SignupRequest.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .name("testName")
                .countryCode("KR")
                .build();

        willThrow(new ValueDuplicatedException(ErrorCode.USER_ON_DELETE))
                .given(userService).join(any(UserDto.SignupRequest.class));

        // when
        ResultActions actions = mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C006"));
    }

    @Test
    public void signin_Correct_Status200AndTokens() throws Exception {
        // given
        UserDto.SigninRequest request = UserDto.SigninRequest.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .build();
        User user = mock(User.class);

        given(userService.getMatchedUser(anyString(), anyString())).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(user.getRoles()).willReturn(Collections.singletonList("USER"));

        // when
        ResultActions actions = mvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        actions.andExpect(status().isOk())
                .andExpect(header().exists(JwtTokenProvider.ACCESS_TOKEN_HEADER))
                .andExpect(header().exists(JwtTokenProvider.REFRESH_TOKEN_HEADER));
    }

    @Test
    public void signin_InvalidParams_Status400AndC001() throws Exception {
        // given
        UserDto.SigninRequest request = UserDto.SigninRequest.builder()
                .build();

        // when
        ResultActions actions = mvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        verify(userService, times(0)).getMatchedUser(anyString(), anyString());
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    public void signin_InvalidParams_Status400AndC002() throws Exception {
        // given
        UserDto.SigninRequest request = UserDto.SigninRequest.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .build();

        willThrow(new ValueDuplicatedException(ErrorCode.USER_DUPLICATED))
                .given(userService).getMatchedUser(anyString(), anyString());

        // when
        ResultActions actions = mvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C002"));
    }

    @Test
    public void signin_InvalidParams_Status400AndC004() throws Exception {
        // given
        UserDto.SigninRequest request = UserDto.SigninRequest.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .build();

        willThrow(new ValueNotMatchException(ErrorCode.PASSWORD_NOT_MATCH))
                .given(userService).getMatchedUser(anyString(), anyString());

        // when
        ResultActions actions = mvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C004"));
    }

    @Test
    public void signin_DeletedUser_Status400AndC006() throws Exception {
        // given
        UserDto.SigninRequest request = UserDto.SigninRequest.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .build();

        willThrow(new ValueNotMatchException(ErrorCode.USER_ON_DELETE))
                .given(userService).getMatchedUser(anyString(), anyString());

        // when
        ResultActions actions = mvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C006"));
    }

    @Test
    public void oauthFacebook_Correct_Status200AndToken() throws Exception {
        // given
        UserDto.OAuthTokenInfo request = UserDto.OAuthTokenInfo.builder()
                .accessToken("token")
                .build();
        User user = mock(User.class);
        JsonNode jsonNode = objectMapper.valueToTree(UserDto.FacebookUserInfo.builder()
                .email("aaa@bb.ccc")
                .name("park")
                .build());

        given(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .willReturn(ResponseEntity.ok(jsonNode));
        given(userRepository.findByEmail(anyString()))
                .willReturn(user);
        given(user.getId()).willReturn(1L);
        given(user.getRoles()).willReturn(Collections.singletonList("USER"));

        // when
        ResultActions actions = mvc.perform(post("/auth/facebook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        actions.andExpect(status().isOk())
                .andExpect(header().exists(JwtTokenProvider.ACCESS_TOKEN_HEADER))
                .andExpect(header().exists(JwtTokenProvider.REFRESH_TOKEN_HEADER));
    }

    @Test
    public void oauthFacebook_JsonParseFail_Status400AndB001() throws Exception {
        // given
        UserDto.OAuthTokenInfo request = UserDto.OAuthTokenInfo.builder()
                .accessToken("token")
                .build();

        given(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .willReturn(ResponseEntity.ok(null));
        given(objectMapper.treeToValue(any(), eq(UserDto.FacebookUserInfo.class)))
                .willThrow(JsonProcessingException.class);

        // when
        ResultActions actions = mvc.perform(post("/auth/facebook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("B001"));
    }

    @Test
    public void oauthFacebook_facebookResponseError_Status400AndB001() throws Exception {
        // given
        UserDto.OAuthTokenInfo request = UserDto.OAuthTokenInfo.builder()
                .accessToken("token")
                .build();

        given(restTemplate.exchange(anyString(), any(), any(), eq(JsonNode.class)))
                .willThrow(RestClientException.class);

        // when
        ResultActions actions = mvc.perform(post("/auth/facebook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("B001"));
    }

    @Test
    public void refreshToken_Correct_Status200AndToken() throws Exception {
        // given
        String refreshToken = "refreshToken";

        given(redisService.getAccessTokenOrThrow(anyString()))
                .willReturn("newToken");

        // when
        ResultActions actions = mvc.perform(post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtTokenProvider.REFRESH_TOKEN_HEADER, refreshToken));

        // then
        actions.andExpect(status().isOk())
                .andExpect(header().exists(JwtTokenProvider.ACCESS_TOKEN_HEADER))
                .andExpect(header().doesNotExist(JwtTokenProvider.REFRESH_TOKEN_HEADER));
    }

    @Test
    public void refreshToken_EmptyRefreshToken_Status400AndC001() throws Exception {
        // given
        String refreshToken = "";

        // when
        ResultActions actions = mvc.perform(post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtTokenProvider.REFRESH_TOKEN_HEADER, refreshToken));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    public void refreshToken_BannedToken_Status400AndC005() throws Exception {
        // given
        String refreshToken = "refreshToken";

        given(redisService.getAccessTokenOrThrow(anyString()))
                .willThrow(new ValueNotMatchException(ErrorCode.TOKEN_INVALID));

        // when
        ResultActions actions = mvc.perform(post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .header(JwtTokenProvider.REFRESH_TOKEN_HEADER, refreshToken));

        // then
        actions.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("C005"));
    }
}