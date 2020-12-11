package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.repository.ContentRepository;
import com.devpedia.watchapedia.service.ContentService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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
}
