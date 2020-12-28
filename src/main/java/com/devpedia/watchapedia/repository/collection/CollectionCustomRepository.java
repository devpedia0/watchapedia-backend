package com.devpedia.watchapedia.repository.collection;

import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;

import java.util.List;

public interface CollectionCustomRepository {

    /**
     * 컬렉션에 포함된 총 컨텐츠 개수를 구한다.
     * @param id 컬렉션 아이디
     * @return 컨텐츠 개수
     */
    Long countContentById(Long id);

    /**
     * user_id = 1에 해당하는 유저을 왓챠피디아 어드민으로보고
     * 해당 유저가 가지고 있는 컬렉션을 왓챠피디아 컬렉션으로 한다.
     * 왓챠피디아 컬렉션을 컨텐츠 타입별로 가지고 온다.
     * @param type 컨텐츠 타입 Enum
     * @return 왓챠피디아 지정 컬렉션 리스트
     */
    List<Collection> getAward(ContentTypeParameter type);

    /**
     * 랜덤으로 해당 컨텐츠 타입에 해당하는 컬렉션을
     * 입력한 개수만큼 리스트로 얻는다.
     * @param type 컨텐츠 타입 Enum
     * @param size 랜덤으로 얻을 컬렉션 개수
     * @return 랜덤 컬렉션 리스트
     */
    List<Collection> getRandom(ContentTypeParameter type, int size);
}
