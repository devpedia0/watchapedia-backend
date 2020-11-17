package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.ValueDuplicatedException;
import com.devpedia.watchapedia.exception.ValueNotMatchException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.exception.common.ErrorField;
import com.devpedia.watchapedia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void join(User user) {
        if (isDuplicated(user.getEmail())) throw new ValueDuplicatedException(ErrorCode.USER_DUPLICATED,
                ErrorField.of("email", user.getEmail(), ""));

        userRepository.save(user);
    }

    public void joinOAuthIfNotExist(String email, String name) {
        if (isDuplicated(email)) return;
        User user = User.builder()
                .email(email)
                .password(UUID.randomUUID().toString())
                .name(name)
                .countryCode("KR")
                .build();
        userRepository.save(user);
    }

    private boolean isDuplicated(String email) {
        User user = userRepository.findByEmail(email);
        return user != null;
    }

    public User getMatchedUser(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null)
            throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new ValueNotMatchException(ErrorCode.PASSWORD_NOT_MATCH);

        return user;
    }

    public UserDto.EmailCheckResult isExistEmail(String email) {
        return UserDto.EmailCheckResult.builder()
                .isExist(isDuplicated(email))
                .build();
    }

    public UserDto.UserInfo getUserInfo(Long id) {
        User user = userRepository.findById(id);
        if (user == null) throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        return UserDto.UserInfo.builder()
                .name(user.getName())
                .email(user.getEmail())
                .countryCode(user.getCountryCode())
                .description(user.getDescription())
                .isEmailAgreed(user.getIsEmailAgreed())
                .isPushAgreed(user.getIsPushAgreed())
                .isSmsAgreed(user.getIsSmsAgreed())
                .accessRange(user.getAccessRange())
                .roles(user.getRoles())
                .build();
    }
}
