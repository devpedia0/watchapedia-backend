package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.MovieDto;
import com.devpedia.watchapedia.dto.TvShowDto;
import com.devpedia.watchapedia.service.ContentService;
import com.devpedia.watchapedia.service.TvShowService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TvShowController {

    private final TvShowService tvShowService;

    /**
     * 어드민용 TV쇼 삽입 API
     * @param request TV쇼 정보에 해당하는 데이터
     * @param poster TV쇼 포스터 이미지 파일
     * @param gallery TV쇼 갤러리 이미지 파일
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "Models -> TvShowInsertRequest 참조", required = true)
    })
    @PostMapping(value = "/admin/tv_shows", consumes = {MediaType.MULTIPART_MIXED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public void addMovie(@RequestPart("body") TvShowDto.TvShowInsertRequest request,
                         @RequestPart("poster") MultipartFile poster,
                         @RequestPart(value = "gallery", required = false) List<MultipartFile> gallery) {
        tvShowService.saveWithImage(request, poster, gallery);
    }

    /**
     * 평점이 4.0 이상인 TV쇼 리스트를 반환한다.
     * @return 평점이 높은 TV쇼 리스트
     */
    @GetMapping("/public/tv_shows/score")
    public List<ContentDto.MainList> getHighScoreList() {
        return tvShowService.getHighScoreList();
    }

    /**
     * 작품을 가장 많이 연기한 배우의 작품을 구한다.
     * @return 화제의 인물 작품 리스트
     */
    @GetMapping("/public/tv_shows/popular")
    public List<ContentDto.MainList> getPopularList() {
        return tvShowService.getPopularList();
    }

    /**
     * 랜덤으로 정해진 태그에 해당하는 TV쇼 리스트 2개를 반환한다.
     * @return 랜덤 태그 TV쇼 리스트
     */
    @GetMapping("/public/tv_shows/tag")
    public List<ContentDto.MainList> getTagList() {
        return tvShowService.getTagList();
    }

    /**
     * 랜덤으로 정해진 유저 컬렉션에 해당하는 TV쇼 리스트 3개를 반환한다.
     * @return 랜덤 컬렉션 TV쇼 리스트
     */
    @GetMapping("/public/tv_shows/collection")
    public List<ContentDto.MainListForCollection> getCollectionList() {
        return tvShowService.getCollectionList();
    }

    /**
     * 왓챠피디아가 지정한 TV쇼 컬렉션을 반환한다.
     * @return 왓챠피디아 TV쇼 컬렉션
     */
    @GetMapping("/public/tv_shows/award")
    public List<ContentDto.ListForAward> getAwardList() {
        return tvShowService.getAwardList();
    }
}
