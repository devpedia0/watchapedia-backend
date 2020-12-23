package com.devpedia.watchapedia.builder;

import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.User;

public class CollectionMother {

    public static Collection.CollectionBuilder defaultCollection(User user) {
        return Collection.builder()
                .description("desc")
                .title("title")
                .user(user);
    }
}
