package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.dto.MovieDto;
import com.devpedia.watchapedia.service.MovieService;
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
public class MovieController {

    private final MovieService movieService;

    /**
     * 어드민용 영화 삽입 API
     * @param request 영화 정보에 해당하는 데이터
     * @param poster 영화 포스터 이미지 파일
     * @param gallery 영화 갤러리 이미지 파일
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "Models -> MovieInsertRequest 참조", required = true)
    })
    @PostMapping(value = "/admin/movies", consumes = {MediaType.MULTIPART_MIXED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public void addMovie(@RequestPart("body") MovieDto.MovieInsertRequest request,
                         @RequestPart("poster") MultipartFile poster,
                         @RequestPart(value = "gallery", required = false) List<MultipartFile> gallery) {
        movieService.saveWithImage(request, poster, gallery);
    }

    /**
     * 평점이 4.0 이상인 영화 리스트를 반환한다.
     * @return 평점이 높은 영화 리스트
     */
    @GetMapping("/public/movies/score")
    public List<ContentDto.MainList> getHighScoreList() {
        return movieService.getHighScoreList();
    }

    /**
     * 작품을 가장 많이 찍은 배우와 감독의 작품을 구한다.
     * @return 화제의 인물 작품 리스트
     */
    @GetMapping("/public/movies/popular")
    public List<ContentDto.MainList> getPopularList() {
        return movieService.getPopularList();
    }

    /**
     * 랜덤으로 정해진 태그에 해당하는 영화 리스트 1개를 반환한다.
     * @return 랜덤 태그 영화 리스트
     */
    @GetMapping("/public/movies/tag")
    public List<ContentDto.MainList> getTagList() {
        return movieService.getTagList();
    }

    /**
     * 랜덤으로 정해진 유저 컬렉션에 해당하는 영화 리스트 3개를 반환한다.
     * @return 랜덤 컬렉션 영화 리스트
     */
    @GetMapping("/public/movies/collection")
    public List<ContentDto.MainListForCollection> getCollectionList() {
        return movieService.getCollectionList();
    }

    /**
     * 왓챠피디아가 지정한 영화 컬렉션을 반환한다.
     * @return 왓챠피디아 영화 컬렉션
     */
    @GetMapping("/public/movies/award")
    public List<ContentDto.ListForAward> getAwardList() {
        return movieService.getAwardList();
    }
}
