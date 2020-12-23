package com.devpedia.watchapedia.service;

import com.devpedia.watchapedia.domain.Tag;
import com.devpedia.watchapedia.dto.TagDto;
import com.devpedia.watchapedia.exception.EntityNotExistException;
import com.devpedia.watchapedia.exception.common.ErrorCode;
import com.devpedia.watchapedia.repository.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
        Optional<Tag> optionalTag = tagRepository.findById(id);
        Tag tag = optionalTag.orElseThrow(() -> new EntityNotExistException(ErrorCode.ENTITY_NOT_FOUND));
        tagRepository.delete(tag);
    }

    public List<TagDto.TagInfo> searchWithPaging(String query, Pageable pageable) {
        List<Tag> list = tagRepository.findByDescriptionContaining(query, pageable);

        return list.stream()
                .map(TagDto.TagInfo::new)
                .collect(Collectors.toList());
    }
}
