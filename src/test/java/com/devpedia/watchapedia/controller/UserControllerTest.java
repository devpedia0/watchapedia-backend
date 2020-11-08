package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.exception.handler.BusinessExceptionHandler;
import com.devpedia.watchapedia.exception.handler.CommonExceptionHandler;
import com.devpedia.watchapedia.repository.RedisRepository;
import com.devpedia.watchapedia.repository.UserRepository;
import com.devpedia.watchapedia.security.JwtTokenProvider;
import com.devpedia.watchapedia.service.RedisService;
import com.devpedia.watchapedia.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RedisRepository redisRepository;
    @MockBean
    private RedisService redisService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void signup_CorrectInput_status201() throws Exception {
        // given
        UserDto.SignupRequest request = UserDto.SignupRequest.builder()
                .email("aaa@bb.ccc")
                .password("1234")
                .name("testName")
                .countryCode("KR")
                .build();

        // when
        ResultActions actions = mvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        actions.andExpect(status().isCreated());
    }
}