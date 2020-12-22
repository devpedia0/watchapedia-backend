package com.devpedia.watchapedia.repository;

import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ElasticSearchRepository {

    public static final String TYPE_TOP_RESULT = "top";
    public static final String TYPE_MOVIE = "M";
    public static final String TYPE_TV_SHOW = "S";
    public static final String TYPE_BOOK = "B";

    public static final String ELASTIC_INDEX = "contents";
    public static final String ELASTIC_COLUMN_CONTENT_ID = "id";
    public static final String ELASTIC_QUERY_FIELD_TITLE = "mainTitle";
    public static final String ELASTIC_QUERY_FIELD_PARTICIPANT = "mainTitle";
    public static final String ELASTIC_QUERY_FIELD_TYPE = "dtype";
    public static final String ELASTIC_QUERY_FIELD_SCORE = "_score";

    private final RestHighLevelClient client;

    /**
     * 엘라스틱 서치에 형태소 분석되어 있는 자료를 검색해서 가져온 후
     * 해당 작품들의 ID 값을 반환한다.
     * 메인제목과 해당 컨텐츠의 참여자 이름에서 검색함.
     * @param type 컨텐츠 타입(M, B, S)
     * @param query 검색어
     * @param page 페이지
     * @param size 사이즈
     * @return 검색결과 컨텐츠 ID
     */
    public List<Long> searchTypeContentsReturnIds(String type, String query, int page, int size) throws IOException {
        SearchRequest request = getSearchRequest(type, query, page, size);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        return extractIdsFromResponse(response);
    }

    /**
     * 모든 컨텐츠 타입 별로 멀티리퀘스트를 날려 검색한다.
     * 엘라스틱 서치에 형태소 분석되어 있는 자료를 검색해서 가져온 후
     * 해당 작품들의 ID 값을 반환한다.
     * 메인제목과 해당 컨텐츠의 참여자 이름에서 검색함.
     * @param query 검색어
     * @param page 페이지
     * @param size 사이즈
     * @return 검색결과 컨텐츠 ID Map
     */
    public Map<String, List<Long>> searchAllContentsReturnIds(String query, int page, int size) throws IOException {
        MultiSearchRequest multiRequest = new MultiSearchRequest();
        multiRequest.add(getSearchRequest(null, query, page, size));
        multiRequest.add(getSearchRequest(ContentTypeParameter.MOVIES.getDtype(), query, page, size));
        multiRequest.add(getSearchRequest(ContentTypeParameter.TV_SHOWS.getDtype(), query, page, size));
        multiRequest.add(getSearchRequest(ContentTypeParameter.BOOKS.getDtype(), query, page, size));

        MultiSearchResponse multiResponse = client.msearch(multiRequest, RequestOptions.DEFAULT);

        Map<String, List<Long>> ids = new HashMap<>();

        MultiSearchResponse.Item[] responses = multiResponse.getResponses();

        if (responses.length > 0)
            ids.put(TYPE_TOP_RESULT, extractIdsFromResponse(responses[0].getResponse()));
        if (responses.length > 1)
            ids.put(TYPE_MOVIE, extractIdsFromResponse(responses[1].getResponse()));
        if (responses.length > 2)
            ids.put(TYPE_TV_SHOW, extractIdsFromResponse(responses[2].getResponse()));
        if (responses.length > 3)
            ids.put(TYPE_BOOK, extractIdsFromResponse(responses[3].getResponse()));

        return ids;
    }

    /**
     * 검색 결과에서 컨텐츠의 ID 값을 추출해낸다.
     * @param response 엘라스틱 서치 검색 응답
     * @return 컨텐츠 ID List
     */
    private List<Long> extractIdsFromResponse(SearchResponse response) {
        return Arrays.stream(response.getHits().getHits())
                .map(hit -> Long.valueOf((Integer)hit.getSourceAsMap().get(ELASTIC_COLUMN_CONTENT_ID)))
                .collect(Collectors.toList());
    }

    /**
     * 엘라스틱 서치에 보낼 요청을 빌더로 만들어 반환한다.
     * 메인제목과 참여자의 이름에서 검색한다.
     * 메인제목에 검색 스코어 가중치를 3배로 한다.
     * @param type 컨텐츠 타입(M, B, S) null 이면 전체 조회
     * @param query 검색어
     * @param page 페이지
     * @param size 사이즈
     * @return 엘라스틱 서치 요청 객체
     */
    private SearchRequest getSearchRequest(String type, String query, int page, int size) {
        SearchRequest request = new SearchRequest(ELASTIC_INDEX);
        SearchSourceBuilder builder = new SearchSourceBuilder();

        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        bool.must(QueryBuilders.multiMatchQuery(query)
                .field(ELASTIC_QUERY_FIELD_TITLE, 3)
                .field(ELASTIC_QUERY_FIELD_PARTICIPANT));

        if (type != null)
            bool.filter(QueryBuilders.matchQuery(ELASTIC_QUERY_FIELD_TYPE, type));

        SortBuilder<?> sort = SortBuilders.fieldSort(ELASTIC_QUERY_FIELD_SCORE);
        sort.order(SortOrder.DESC);

        builder.query(bool)
                .from((page - 1) * size)
                .size(size)
                .sort(sort);

        request.source(builder);

        return request;
    }

}
