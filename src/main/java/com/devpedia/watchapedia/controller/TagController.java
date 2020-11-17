package com.devpedia.watchapedia.controller;

import com.devpedia.watchapedia.dto.TagDto;
import com.devpedia.watchapedia.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping("/admin/tags")
    public void addTag(@RequestBody @Valid TagDto.TagInsertRequest request) {
        tagService.addTag(request);
    }

    @DeleteMapping("/admin/tags/{id}")
    public void deleteTag(@PathVariable("id") Long tagId) {
        tagService.delete(tagId);
    }

    @GetMapping("/tags")
    public List<TagDto.TagInfo> getTags(@RequestParam(required = false) String search,
                                        @RequestParam @Positive int page,
                                        @RequestParam @Min(1)@Max(40) int size) {
        return tagService.searchWithPaging(search, page, size);
    }
}
