package com.devpedia.watchapedia.exception.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

    // A: Etc
    ACCESS_NOT_AVAILABLE(BAD_REQUEST, "A001", "조회할 권한이 없습니다."),
    CONTENT_TYPE_NOT_VALID(INTERNAL_SERVER_ERROR, "A002", "Class 타입 변환 실패"),

    // B: IO Fail
    OAUTH_PROVIDER_FAIL(BAD_REQUEST, "B001", "OAuth인증에 실패했습니다"),

    // C: Client error
    INPUT_VALUE_INVALID(BAD_REQUEST, "C001", "적절하지 않은 입력값이 있습니다"),
    USER_DUPLICATED(BAD_REQUEST, "C002", "이미 존재하는 회원입니다"),
    ENTITY_NOT_FOUND(BAD_REQUEST, "C003", "해당 엔티티가 존재하지 않습니다"),
    PASSWORD_NOT_MATCH(BAD_REQUEST, "C004", "비밀번호가 일치하지 않습니다"),
    TOKEN_INVALID(UNAUTHORIZED, "C005", "유효하지 않은 토큰입니다"),
    USER_ON_DELETE(BAD_REQUEST, "C006", "삭제 유예기간인 회원입니다"),
    IMAGE_FORMAT_INVALID(BAD_REQUEST, "C007", "올바르지 않은 이미지 파일입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
