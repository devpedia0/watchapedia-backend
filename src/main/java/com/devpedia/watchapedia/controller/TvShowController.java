package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.MovieDto;
import com.devpedia.watchapedia.dto.TvShowDto;
import com.devpedia.watchapedia.service.ContentService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TvShowController {

    private final ContentService contentService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "Models -> TvShowInsertRequest 참조", required = true)
    })
    @PostMapping(value = "/admin/tv_shows", consumes = {MediaType.MULTIPART_MIXED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public void addMovie(@RequestPart("body") TvShowDto.TvShowInsertRequest request,
                         @RequestPart("poster") MultipartFile poster,
                         @RequestPart(value = "gallery", required = false) List<MultipartFile> gallery) {
        contentService.saveTvShowWithImage(request, poster, gallery);
    }
}
