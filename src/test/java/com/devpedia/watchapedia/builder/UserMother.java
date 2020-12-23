package com.devpedia.watchapedia.builder;

import com.devpedia.watchapedia.domain.User;

public class UserMother {

    public static User.UserBuilder defaultUser() {
        return User.builder()
                .email("aa@bbb.ccc")
                .password("1234")
                .name("testName")
                .countryCode("KR");
    }
}
