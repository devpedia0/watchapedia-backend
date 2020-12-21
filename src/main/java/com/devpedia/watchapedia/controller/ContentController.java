package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.enums.ContentTypeParameter;
import com.devpedia.watchapedia.exception.ExternalIOException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.content.ContentRepository;
import com.devpedia.watchapedia.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class ContentController {

    private final ContentService contentService;
    private final ContentRepository contentRepository;

    /**
     * 전체 평점 개수를 조회한다.
     * @return 전체 평점 개수
     */
    @GetMapping("/public/contents/scores/count")
    public Map<String, Long> getTotalScoreCount() {
        return Map.of("totalCount", contentRepository.countTotalScores());
    }

    /**
     * 트렌드 작품 타이틀 리스트를 조회한다.
     * 조회방식은 임의로 정해졌으며 추후 수정가능.
     * @return 트렌드 작품 타이틀 리스트
     */
    @GetMapping("/public/contents/trending_words")
    public List<String> getTrendingWords() {
        return contentRepository.getTrendingWords(5);
    }

    /**
     * 왓챠피디아 컬렉션 상세 내역 조회.
     * @param id 컬렉션 아이디
     * @param size 최초 컨텐츠 목록 사이즈
     * @return 컬렉션 정보 및 컨텐츠 리스트
     */
    @GetMapping("/public/awards/{id}")
    public ContentDto.MainList getStaffMadeInfo(@PathVariable Long id, @RequestParam @Min(1)@Max(20) int size) {
        return contentService.getAwardDetail(id, 1, size);
    }

    /**
     * 왓챠피디아 컬렉션 내의 컨텐츠 조회.
     * @param id 컬렉션 아이디
     * @param size 사이즈
     * @return 컬렉션 컨텐츠 리스트
     */
    @GetMapping("/public/awards/{id}/contents")
    public List<ContentDto.MainListItem> getStaffMadeContents(@PathVariable Long id,
                                                              @RequestParam @Positive int page,
                                                              @RequestParam @Min(1)@Max(20) int size) {
        return contentService.getAwardDetail(id, page, size).getList();
    }

    /**
     * 유저 컬렉션 상세 내역 조회.
     * @param id 컬렉션 아이디
     * @param size 최초 컨텐츠 목록 사이즈
     * @return 컬렉션 정보 및 컨텐츠 리스트
     */
    @GetMapping("/public/collections/{id}")
    public ContentDto.CollectionDetail getCollectionInfo(@PathVariable Long id, @RequestParam @Min(1)@Max(20) int size) {
        return contentService.getCollectionDetail(id, 1, size);
    }

    /**
     * 왓챠피디아 유저 내의 컨텐츠 조회.
     * @param id 컬렉션 아이디
     * @param size 사이즈
     * @return 컬렉션 컨텐츠 리스트
     */
    @GetMapping("/public/collections/{id}/contents")
    public List<ContentDto.CollectionItem> getCollectionContents(@PathVariable Long id,
                                                               @RequestParam @Positive int page,
                                                               @RequestParam @Min(1)@Max(20) int size) {
        return contentService.getCollectionDetail(id, page, size).getList();
    }

    /**
     * 통합 검색 결과를 반환한다.
     * - 전체 중 상위 결과
     * - 영화
     * - 티비쇼
     * - 책
     * - 유저
     * @param query 검색어
     * @return 통합 검색 결과
     */
    @GetMapping("/public/searches")
    public ContentDto.SearchResult search(@RequestParam @NotBlank String query) {
        try {
            return contentService.getSearchResult(query);
        } catch (IOException e) {
            throw new ExternalIOException(ErrorCode.ELASTIC_SEARCH_FAIL);
        }
    }

    /**
     * 각 컨텐츠 타입에 해당하는 검색 결과를 반환한다.
     * @param contentType 컨텐츠 타입
     * @param query 검색어
     * @param page 페이지
     * @param size 사이즈
     * @return 검색 결과(SearchMovieItem, SearchTvShowItem, SearchBookItem)
     */
    @GetMapping("/public/searches/{contentType}")
    public List<Object> searchByType(@PathVariable ContentTypeParameter contentType,
                                     @RequestParam @NotBlank String query,
                                     @RequestParam @Positive int page,
                                     @RequestParam @Min(1)@Max(20) int size) {
        try {
            return contentService.searchByType(contentType, query, page, size);
        } catch (IOException e) {
            throw new ExternalIOException(ErrorCode.ELASTIC_SEARCH_FAIL);
        }
    }
}
