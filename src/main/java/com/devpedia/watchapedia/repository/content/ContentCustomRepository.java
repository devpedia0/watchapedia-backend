package com.devpedia.watchapedia.repository.content;

import com.devpedia.watchapedia.domain.Collection;
import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.domain.Tag;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ContentCustomRepository {

    /**
     * 평점 평균이 지정 점수 이상인 컨텐츠를 개수만큼 조회한다.
     * @param score 조회할 평점 기준
     * @param size 반환 개수
     * @return 평점이 이상인 컨텐츠 리스트
     */
    List<Content> getContentsScoreIsGreaterThan(ContentTypeParameter type, double score, int size);

    /**
     * 해당 id로 조회한 컨텐츠들의 평균 평점을 맵 형태로 반환한다.
     * @param ids PK set
     * @return key: id, value: 평균 평점
     */
    Map<Long, Double> getContentScore(Set<Long> ids);

    /**
     * 해당 인물이 어떠한 역할로든 참여한 컨텐츠를 개수만큼 조회한다.
     * @param type 컨텐츠 종류 Enum
     * @param participantId 컨텐츠에 참여한 인물 PK
     * @param size 반환 개수
     * @return 인물이 참여한 컨텐츠 리스트
     */
    List<Content> getContentsHasParticipant(ContentTypeParameter type, Long participantId, int size);

    /**
     * 해당 태그가 포함된 컨텐츠를 개수만큼 반환한다.
     * @param type 컨텐츠 종류 Enum
     * @param tagId 컨텐츠에 걸린 태그 PK
     * @param size 반환 개수
     * @return 태그가 걸린 컨텐츠 리스트
     */
    List<Content> getContentsTagged(ContentTypeParameter type, Long tagId, int size);

    /**
     * 해당 컬렉션에 포함되는 컨텐츠를 개수만큼 반환한다.
     * @param collectionId 컬렉션 PK
     * @param pageable 페이징
     * @return 컬렉션에 담긴 컨텐츠 리스트
     */
    List<Content> getContentsInCollection(Long collectionId, Pageable pageable);

    /**
     * 코멘트가 갯수가 많은 순으로 size 만큼 조회한다
     * @param size 사이즈
     * @return 트렌트 컨텐츠 제목
     */
    List<String> getTrendingWords(int size);

    /**
     * 프록시(HibernateProxy) 객체를 실제 컨텐츠 엔티티로 언프록시 한다
     * @param entity unproxy 할 컨텐츠 엔티티
     * @return unproxy 된 컨텐츠 엔티티
     */
    <T extends Content> T initializeAndUnproxy(T entity);
}
