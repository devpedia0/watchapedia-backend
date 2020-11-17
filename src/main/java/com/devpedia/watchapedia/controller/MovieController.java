package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.MovieDto;
import com.devpedia.watchapedia.service.ContentService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MovieController {

    private final ContentService contentService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "Request 객체 json을 body에 담 Content-Type을 application/json로 해야함",
                    required = true, paramType = "formData")
    })
    @PostMapping(value = "/admin/movies", consumes = {MediaType.MULTIPART_MIXED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public void addMovie(@RequestPart("body") MovieDto.MovieInsertRequest request, @RequestPart("file") MultipartFile file) {
        contentService.saveMovieWithImage(request, file);
    }
}
