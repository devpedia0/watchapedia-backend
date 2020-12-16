package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.repository.ContentRepository;
import com.devpedia.watchapedia.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final ContentRepository contentRepository;

    /**
     * 전체 평점 개수를 조회한다.
     * @return 전체 평점 개수
     */
    @GetMapping("/public/contents/scores/count")
    public Map<String, Long> getTotalScoreCount() {
        return Map.of("totalCount", contentRepository.getTotalScoreCount());
    }

    /**
     * 트렌드 작품 타이틀 리스트를 조회한다.
     * 조회방식은 임의로 정해졌으며 추후 수정가능.
     * @return 트렌드 작품 타이틀 리스트
     */
    @GetMapping("/public/contents/trending_words")
    public List<String> getTrendingWords() {
        return contentRepository.getTrendingWords(50, 5);
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
}
