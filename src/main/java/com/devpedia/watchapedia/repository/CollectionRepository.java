package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.domain.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CollectionRepository {

    private final EntityManager em;

    /**
     * 랜덤으로 해당 컨텐츠 타입에 해당하는 컬렉션을
     * 입력한 개수만큼 리스트로 얻는다.
     * @param type 컨텐츠 타입 String(M: 영화, B: 책, S:TV)
     * @param size 랜덤으로 얻을 컬렉션 개수
     * @return 랜덤 컬렉션 리스트
     */
    public List<Collection> getRandom(String type, int size) {
        return em.createNativeQuery(
                "select c2.* " +
                        "from collection_content cc " +
                        "join content c on cc.cotent_id = c.content_id " +
                        "join collection c2 on cc.collection_id = c2.collection_id " +
                        "and c.dtype = :type " +
                        "group by cc.collection_id " +
                        "order by rand() " +
                        "limit :size", Collection.class)
                .setParameter("type", type)
                .setParameter("size", size)
                .getResultList();
    }

    /**
     * user_id = 1에 해당하는 유저을 왓챠피디아 어드민으로보고
     * 해당 유저가 가지고 있는 컬렉션을 왓챠피디아 컬렉션으로 한다.
     * 왓챠피디아 컬렉션을 컨텐츠 타입별로 가지고 온다.
     * @param type 컨텐츠 타입 String(M: 영화, B: 책, S:TV)
     * @return 왓챠피디아 지정 컬렉션 리스트
     */
    public List<Collection> getAward(String type) {
        return em.createQuery(
                "select c " +
                        "from Collection c " +
                        "join CollectionContent cc on c.id = cc.collection.id " +
                        "join cc.content ct " +
                        "where c.user.id = 1" +
                        "and ct.dtype = :type " +
                        "group by cc.collection", Collection.class)
                .setParameter("type", type)
                .getResultList();
    }
}
