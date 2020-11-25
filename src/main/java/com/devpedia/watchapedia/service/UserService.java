package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.CollectionContent;
import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.domain.User;
import com.devpedia.watchapedia.dto.UserDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.ValueDuplicatedException;
import com.devpedia.watchapedia.exception.ValueNotMatchException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.exception.common.ErrorField;
import com.devpedia.watchapedia.repository.ContentRepository;
import com.devpedia.watchapedia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final PasswordEncoder passwordEncoder;

    public void join(UserDto.SignupRequest request) {
        User existUser = userRepository.findByEmail(request.getEmail());

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

    public void joinOAuthIfNotExist(String email, String name) {
        User existUser = userRepository.findByEmail(email);

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

    public User getMatchedUser(String email, String password) {
        User user = getUserIfExistOrThrow(email);
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new ValueNotMatchException(ErrorCode.PASSWORD_NOT_MATCH);

        return user;
    }

    public UserDto.EmailCheckResult isExistEmail(String email) {
        return UserDto.EmailCheckResult.builder()
                .isExist(isExistUser(email))
                .build();
    }

    private boolean isExistUser(String email) {
        User user = userRepository.findByEmail(email);
        return user != null;
    }

    public UserDto.UserInfo getUserInfo(Long id) {
        User user = getUserIfExistOrThrow(id);
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

    public void editUserInfo(Long id, UserDto.UserInfoEditRequest userInfo) {
        User user = getUserIfExistOrThrow(id);

        if (userInfo.getName() != null && !StringUtils.isBlank(userInfo.getName()))
            user.setName(userInfo.getName());
        if (userInfo.getDescription() != null) user.setDescription(userInfo.getDescription());
        if (userInfo.getCountryCode() != null) user.setCountryCode(userInfo.getCountryCode());
        if (userInfo.getAccessRange() != null) user.setAccessRange(userInfo.getAccessRange());
        if (userInfo.getIsEmailAgreed() != null) user.setEmailAgreed(userInfo.getIsEmailAgreed());
        if (userInfo.getIsPushAgreed() != null) user.setPushAgreed(userInfo.getIsPushAgreed());
        if (userInfo.getIsSmsAgreed() != null) user.setSmsAgreed(userInfo.getIsPushAgreed());
    }

    public void addCollection(Long userId, UserDto.CollectionInsertRequest request) {
        User user = getUserIfExistOrThrow(userId);

        Collection collection = Collection.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        userRepository.save(collection);

        List<Content> contents = contentRepository.findListIn(Content.class, new HashSet<>(request.getContents()));

        for (Content content : contents) {
            CollectionContent collectionContent = CollectionContent.builder()
                    .collection(collection)
                    .content(content)
                    .build();
            userRepository.save(collectionContent);
        }
    }

    public List<UserDto.UserInfoMinimum> getAllUserInfo() {
        List<User> list = userRepository.findAll();
        return list.stream()
                .map(user -> UserDto.UserInfoMinimum.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .roles(user.getRoles())
                        .build())
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        User user = getUserIfExistOrThrow(id);
        user.delete();
    }

    private User getUserIfExistOrThrow(Long id) {
        User user = userRepository.findById(id);
        if (user == null || user.getIsDeleted())
            throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        return user;
    }

    private User getUserIfExistOrThrow(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null || user.getIsDeleted())
            throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        return user;
    }
}
