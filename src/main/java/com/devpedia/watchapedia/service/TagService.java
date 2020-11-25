package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Tag;
import com.devpedia.watchapedia.dto.TagDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public void addTag(TagDto.TagInsertRequest request) {
        Tag tag = Tag.builder()
                .description(request.getDescription())
                .build();
        tagRepository.save(tag);
    }

    public void delete(Long id) {
        Tag tag = tagRepository.findById(id);
        if (tag == null) throw new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND);
        tagRepository.delete(tag);
    }

    public List<TagDto.TagInfo> searchWithPaging(String query, int page, int size) {
        List<Tag> list = tagRepository.searchWithPaging(query, page, size);

        return list.stream()
                .map(tag -> TagDto.TagInfo.builder()
                        .id(tag.getId())
                        .description(tag.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
}
