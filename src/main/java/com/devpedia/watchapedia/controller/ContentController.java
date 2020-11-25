package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.ContentDto;
import com.devpedia.watchapedia.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/admin/contents")
    public List<ContentDto.CommonContentInfo> getContents(@RequestParam(required = false) String query,
                                                          @RequestParam @Positive int page,
                                                          @RequestParam @Min(1)@Max(20) int size) {
        return contentService.searchAllWithPaging(query, page, size);
    }
}
