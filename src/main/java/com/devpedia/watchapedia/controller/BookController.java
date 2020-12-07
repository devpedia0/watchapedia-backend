package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.BookDto;
import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.service.BookService;
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
public class BookController {

    private final BookService bookService;

    /**
     * 어드민용 책 삽입 API
     * @param request 책 정보에 해당하는 데이터
     * @param poster 책 포스터 이미지 파일
     * @param gallery 책 갤러리 이미지 파일
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "Models -> BookInsertRequest 참조", required = true)
    })
    @PostMapping(value = "/admin/books", consumes = {MediaType.MULTIPART_MIXED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public void addBook(@RequestPart("body") BookDto.BookInsertRequest request,
                        @RequestPart("poster") MultipartFile poster,
                        @RequestPart(value = "gallery", required = false) List<MultipartFile> gallery) {
        bookService.saveWithImage(request, poster, gallery);
    }

    /**
     * 평점이 4.0 이상인 책 리스트를 반환한다.
     * @return 평점이 높은 책 리스트
     */
    @GetMapping("/public/books/score")
    public List<ContentDto.MainList> getHighScoreList() {
        return bookService.getHighScoreList();
    }

    /**
     * 작품을 가장 많이 저술한 작가의 작품을 구한다.
     * @return 화제의 인물 작품 리스트
     */
    @GetMapping("/public/books/popular")
    public List<ContentDto.MainList> getPopularList() {
        return bookService.getPopularList();
    }

    /**
     * 랜덤으로 정해진 태그에 해당하는 책 리스트 2개를 반환한다.
     * @return 랜덤 태그 책 리스트
     */
    @GetMapping("/public/books/tag")
    public List<ContentDto.MainList> getTagList() {
        return bookService.getTagList();
    }

    /**
     * 랜덤으로 정해진 유저 컬렉션에 해당하는 책 리스트 3개를 반환한다.
     * @return 랜덤 컬렉션 책 리스트
     */
    @GetMapping("/public/books/collection")
    public List<ContentDto.MainListForCollection> getCollectionList() {
        return bookService.getCollectionList();
    }

    /**
     * 왓챠피디아가 지정한 책 컬렉션을 반환한다.
     * @return 왓챠피디아 책 컬렉션
     */
    @GetMapping("/public/books/award")
    public List<ContentDto.ListForAward> getAwardList() {
        return bookService.getAwardList();
    }

}
