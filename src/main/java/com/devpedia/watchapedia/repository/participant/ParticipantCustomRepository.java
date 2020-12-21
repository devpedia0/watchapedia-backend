package com.devpedia.watchapedia.repository.participant;

import com.devpedia.watchapedia.domain.Content;
import com.devpedia.watchapedia.domain.Participant;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ParticipantCustomRepository {

    /**
     * 해당 컨텐츠 종류에서 특정 직업의 인물 중
     * 가장 많이 작품에 참여한 인물을 구한다. (ex. 영화에 가장 많이 참여한 감독)
     * @param type 컨텐츠 타입 Enum
     * @param job 직업
     * @return 가장 많이 참여한 인물
     */
    Participant findMostFamous(ContentTypeParameter type, String job);
}
