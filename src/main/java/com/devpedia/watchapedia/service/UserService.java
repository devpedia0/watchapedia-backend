package com.devpedia.watchapedia.service;

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

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 유저 회원가입을 한다.
     * 해당 이메일의 유저가 존재하거나 삭제된 회원이면 Exception
     * @param request 유저가입 정보
     */
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

    /**
     * OAuth 진행 시 해당 이메일의 유저가
     * 존재하면 bypass
     * 존재하지 않으면 가입시키고
     * 삭제된 회원이면 Exception
     * @param email 가입 이메일
     * @param name 가입 이름
     */
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
        User user = userRepository.findByEmail(email);
        return user != null;
    }

    /**
     * 유저 이메일, 이름, 설명, 국가코드, 각종 설정, 권한 등을 반환한다.
     * @param id user_id
     * @return 유저 정보
     */
    public UserDto.UserInfo getUserInfo(Long id) {
        User user = getUserIfExistOrThrow(id);
        return new UserDto.UserInfo(user);
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
        if (userInfo.getIsSmsAgreed() != null) user.setSmsAgreed(userInfo.getIsPushAgreed());
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
    private User getUserIfExistOrThrow(Long id) {
        User user = userRepository.findById(id);
        if (user == null || user.getIsDeleted())
            throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        return user;
    }

    /**
     * 해당 이메일로 로 유저를 조회한다.
     * 존재하지 않거나 삭제된 유저라면 Exception
     * @param email 이메일
     * @return 유저
     */
    private User getUserIfExistOrThrow(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null || user.getIsDeleted())
            throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        return user;
    }

    /**
     * 해당 아이디의 유저가 매긴
     * 컨텐츠 별 평가 개수, 보고싶어요 개수를 반환한다.
     * @param id user_id
     * @return 컨텐츠 별 평가 및 보고싶어요 개수
     */
    public UserDto.UserRatingAndWishContent getRatingInfo(Long id) {
        return userRepository.getRatingAndWishCounts(id);
    }
}
