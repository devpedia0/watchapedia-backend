package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.MovieDto;
import com.devpedia.watchapedia.service.ContentService;
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

    private final ContentService contentService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "body", value = "Models -> MovieInsertRequest 참조", required = true)
    })
    @PostMapping(value = "/admin/movies", consumes = {MediaType.MULTIPART_MIXED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public void addMovie(@RequestPart("body") MovieDto.MovieInsertRequest request,
                         @RequestPart("poster") MultipartFile poster,
                         @RequestPart(value = "gallery", required = false) List<MultipartFile> gallery) {
        contentService.saveMovieWithImage(request, poster, gallery);
    }
}
